package me.asu.run;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * IndexSearcher 利用 ripgrep（rg）搜索索引文件，
 * 依据四种规则（完全匹配、前缀匹配、包含匹配、模糊匹配）对索引文件中记录的 name 部分进行搜索，
 * 并返回对应的 path 列表（去重后）。
 */
public class IndexSearcher implements SearchEngine {
    private final String indexFilePath;

    public IndexSearcher(String indexFilePath) {
        this.indexFilePath = indexFilePath;
        if (indexFilePath == null || indexFilePath.isEmpty()) {
            throw new IllegalArgumentException("indexFilePath is null or empty");
        }
        if (!Files.isRegularFile(Paths.get(indexFilePath))) {
           throw new IllegalArgumentException("indexFilePath is not a regular file");
        }
    }

    @Override
    public List<FileInfo> search(Condition condition) {
        if (condition == null) return Collections.emptyList();
        final String keyword = condition.getName();
        try {
            List<String>  result = search(keyword);
            return result.stream().map(p-> FileInfoConverter.convert(p)).collect(Collectors.toList());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return Collections.emptyList();
        }

    }

    /**
     * 根据关键字搜索索引文件，返回匹配的 path 列表。
     * 
     * @param keyword 搜索关键字
     * @return 去重后的匹配路径列表
     * @throws IOException 如果 rg 命令执行失败则抛出异常
     */
    public List<String> search(String keyword) throws IOException {
        Set<String> results = new LinkedHashSet<>();
        List<String> patterns = new ArrayList<>();
        // 规则1：完全匹配（name 完全等于 keyword）
        patterns.add(keyword + "\\s*\\|");
        // 规则3：包含匹配（name 中包含 keyword）
        patterns.add(".*" + keyword + ".*\\|");
        // 规则4：模糊匹配（name 中依次包含 keyword 各字符，可有其他字符间隔）
        patterns.add(buildFuzzyPattern(keyword) + "s\\|");

        for (String pattern : patterns) {
            System.out.printf("Searching for pattern %s\n", pattern);
            List<String> lines = runRg(pattern);
            for (String line : lines) {
                // 索引文件格式： name | path ，取“|”后面的部分
                String[] parts = line.split("\\|", 2);
                if (parts.length == 2) {
                    String path = parts[1].trim();
                    results.add(path);
                }
            }
        }
        return new ArrayList<>(results);
    }

    /**
     * 调用外部命令 rg（ripgrep）搜索指定正则表达式在索引文件中的匹配行。
     */
    private List<String> runRg(String regex) throws IOException {
        List<String> outputLines = new ArrayList<>();
        ProcessBuilder pb = new ProcessBuilder("rg", "-i", "--no-filename", "--regexp", regex, indexFilePath);
        Process process = pb.start();
        try (InputStream is = process.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputLines.add(line);
            }
        }
        try {
            int exitCode = process.waitFor();
            // rg 未匹配到内容时退出码不为 0，此处可忽略
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("rg 命令被中断", e);
        }
        return outputLines;
    }

    /**
     * 构造模糊匹配正则：例如 "word" 转换为 ".*w.*o.*r.*d.*"
     */
    private String buildFuzzyPattern(String keyword) {
        StringBuilder sb = new StringBuilder(".*");
        for (char c : keyword.toCharArray()) {
            sb.append(c).append(".*");
        }
        return sb.toString();
    }
}
