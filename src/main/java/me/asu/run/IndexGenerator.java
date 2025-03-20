package me.asu.run;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * IndexGenerator 遍历指定目录，过滤出可执行程序和脚本，
 * 并生成索引文件（每行格式： name | path ）。
 * 
 * － 对于 Windows：检查文件扩展名（如 exe、bat、cmd、com、ps1）；对于 .lnk 文件，示例中暂不解析（可扩展）。
 * － 对于 Linux：判断文件是否具有执行权限，或检查文件头部是否以 "#!"（脚本）或 ELF 头标识开头。
 * － 对于 link 类型的文件，采用 toRealPath() 追踪真实文件，同时避免循环引用。
 */
public class IndexGenerator {
    private static final Logger logger = Logger.getLogger(IndexGenerator.class.getName());
    // Windows 下常见的可执行扩展名
    private static final Set<String> WINDOWS_EXECUTABLE_EXTENSIONS = new HashSet<>();
    static {
        FileType.BIN.getExtend().forEach(ext -> WINDOWS_EXECUTABLE_EXTENSIONS.add(ext));
        FileType.WIN_SCRIPT.getExtend().forEach(ext -> WINDOWS_EXECUTABLE_EXTENSIONS.add(ext));
        FileType.SCRIPT.getExtend().forEach(ext -> WINDOWS_EXECUTABLE_EXTENSIONS.add(ext));

    }
    private final Set<String> regexExclude = new HashSet<>();
    private final Set<Path> startDirs = new HashSet<>();
    private final Set<Path> excludePaths = new HashSet<>();
    private final Path indexFile;

    public IndexGenerator(String indexFilePath, String[] startDirs, String[] excludePaths) {
        Objects.requireNonNull(indexFilePath);
        Objects.requireNonNull(startDirs);
        for (String startDir : startDirs) {
            this.startDirs.add(Paths.get(startDir).toAbsolutePath());
        }
        for (String excludePath : excludePaths) {
            if (excludePath.contains("*") ||excludePath.contains("?") ) {
                this.regexExclude.add(excludePath);
            } else {
                this.excludePaths.add(Paths.get(excludePath).toAbsolutePath());
            }
        }
        this.indexFile = Paths.get(indexFilePath);
    }

    /**
     * 生成索引文件，遍历目录并将符合条件的文件写入索引文件中。
     */
    public void generateIndex() throws Exception {
        // 使用 BufferedWriter 写入索引文件
        if (!Files.isDirectory(indexFile.toAbsolutePath().getParent())) {
            Files.createDirectories(indexFile.toAbsolutePath().getParent());
        }
        Path tmpFile = indexFile.getParent().resolve(".asu-run.idx.tmp");
        // todo: sort, uniq and append.
        try (BufferedWriter writer = Files.newBufferedWriter(tmpFile, StandardCharsets.UTF_8)) {
            // 使用 Files.walkFileTree 遍历目录，启用 FOLLOW_LINKS 以追踪符号链接
            for (Path startDir : startDirs) {
                Files.walkFileTree(startDir, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                    // 用于防止因链接循环多次访问同一真实路径
                    private final Set<Path> visitedRealPaths = new HashSet<>();

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        try {
                            Path realPath = file.toRealPath();
                            if (visitedRealPaths.contains(realPath)) {
                                logger.warning("跳过循环链接文件：" + file);
                                return FileVisitResult.CONTINUE;
                            }
                            visitedRealPaths.add(realPath);
                            if (excludePaths.contains(file.toAbsolutePath())
                                || excludePaths.contains(realPath.toAbsolutePath())) {
                                return FileVisitResult.CONTINUE;
                            }
                            for (String p : regexExclude) {
                                if(WildcardUtils.matches(p, file.toAbsolutePath().toString())) return FileVisitResult.CONTINUE;
                                if(WildcardUtils.matches(p, file.toString())) return FileVisitResult.CONTINUE;
                                if(WildcardUtils.matches(p, realPath.toAbsolutePath().toString())) return FileVisitResult.CONTINUE;
                                if(WildcardUtils.matches(p, realPath.toString())) return FileVisitResult.CONTINUE;
                            }

                            if (isExecutable(file, realPath, attrs)) {
                                String name = file.getFileName().toString();
                                String absolutePath = file.toAbsolutePath().toString();
                                // 写入格式： name | path
                                writer.write(name + " | " + absolutePath);
                                writer.newLine();
                            }
                        } catch (Exception e) {
                            logger.log(Level.WARNING, "处理文件时出错：" + file, e);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        logger.log(Level.WARNING, "无法访问文件：" + file, exc);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }

            ExternalSortUtil.sortAndUniq(tmpFile.toString(), indexFile.toString());
//            Files.move(tmpFile, indexFile, StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(tmpFile);
            logger.info("索引文件已生成：" + indexFile.toAbsolutePath());
        }

    }


    /**
     * 判断文件是否为可执行或脚本文件。
     * Windows 下：
     *   - 如果文件名以 .lnk 结尾，则解析该 shortcut 文件，获取目标路径，
     *     如果目标路径存在且目标文件的扩展名属于可执行文件，则返回 true；否则返回 false。
     *   - 非 .lnk 文件则依据扩展名判断。
     * Linux 下：
     *   - 判断文件是否具有执行权限，或者检查文件头部是否为脚本（#!）或 ELF 文件。
     *
     * @param file 原始文件路径
     * @param realPath 文件的真实路径（经过符号链接解析）
     * @param attrs 文件属性
     * @return 如果文件符合可执行或脚本规则，则返回 true，否则返回 false
     */
    private boolean isExecutable(Path file, Path realPath, BasicFileAttributes attrs) throws IOException, ParseException {
        if (!attrs.isRegularFile()) {
            return false;
        }
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String fileName = file.getFileName().toString().toLowerCase();
            if (fileName.endsWith(".lnk") ) {
                if (!WindowsShortcut.isPotentialValidLink(file.toFile())) return false;
                // 处理 Windows 快捷方式（.lnk 文件），解析真实目标路径
                WindowsShortcut ws = new WindowsShortcut(file.toFile());
                String targetPathStr = ws.getRealFilename();
                if (targetPathStr == null || targetPathStr.isEmpty()) {
                    return false;
                }
                Path targetPath = Paths.get(targetPathStr);
                if (!Files.exists(targetPath)) {
                    return false;
                }
                // 根据目标文件的扩展名判断是否为可执行程序
                String targetFileName = targetPath.getFileName().toString().toLowerCase();
                int dotIndex = targetFileName.lastIndexOf('.');
                if (dotIndex != -1) {
                    String ext = targetFileName.substring(dotIndex + 1);
                    return WINDOWS_EXECUTABLE_EXTENSIONS.contains(ext);
                }
                return false;
            } else {
                // 非 .lnk 文件，直接依据扩展名判断
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex != -1) {
                    String ext = fileName.substring(dotIndex + 1);
                    return WINDOWS_EXECUTABLE_EXTENSIONS.contains(ext);
                }
                return false;
            }
        } else {
            // Linux/Unix 下：先判断是否有执行权限
            if (Files.isExecutable(realPath)) {
                return true;
            }
            // 检查文件头部是否为脚本（#!）或 ELF 文件（0x7F 'E' 'L' 'F'）
            try (InputStream is = Files.newInputStream(realPath)) {
                byte[] header = new byte[4];
                int read = is.read(header);
                if (read >= 2) {
                    String headerStr = new String(header, 0, read, StandardCharsets.US_ASCII);
                    if (headerStr.startsWith("#!")) {
                        return true;
                    }
                }
                if (read == 4) {
                    if ((header[0] & 0xFF) == 0x7F && header[1] == 'E' &&
                            header[2] == 'L' && header[3] == 'F') {
                        return true;
                    }
                }
            } catch (IOException e) {
                // 读取文件头失败，则返回 false
            }
            return false;
        }
    }



}
