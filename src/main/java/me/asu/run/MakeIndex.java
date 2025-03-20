package me.asu.run;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MakeIndex {



    public static void main(String[] args) throws Exception {
        addPaths(args);

        buildIndex();

    }

    private static void addPaths(String[] args) {
        HandlerPath instance = HandlerPath.getInstance();
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if ("-i".equals(args[i])) {
                    i++;
                    if (i <= args.length) {
                        instance.addIncludePath(args[i]);
                    }
                } else if ("-e".equals(args[i])) {
                    i++;
                    if (i <= args.length) {
                        instance.addExcludePath(args[i]);
                    }
                }
            }
        }

        instance.addExcludePath("*.git");
        instance.addExcludePath("*.cvs");
        instance.addExcludePath("*.svn");
        instance.addExcludePath("*.settings");
        instance.addExcludePath(".*\\.idea");
        instance.addExcludePath(".*/.idea");
        instance.addExcludePath("*\\node_modules");
        instance.addExcludePath("*/node_modules");
        instance.addExcludePath("*\\.m2");
        instance.addExcludePath("*/.m2");
        instance.addExcludePath(".*RECYCLE.BIN.*");
        instance.addExcludePath("*\\.Trash-*");
        instance.addExcludePath("*/.Trash-*");
        instance.addExcludePath("*:\\Users\\*\\AppData\\Roaming\\Microsoft\\Windows\\Recent");

        if (instance.getIncludePath().isEmpty()) {
            if (OsUtils.WINDOWS) {
//                File[] roots = File.listRoots();
//                for (int i = 0; i < roots.length; i++) {
//                    // 磁盘路径
//                    instance.addIncludePath(roots[i].getPath());
//                }
                // 搞点常规的地址就好了。
                instance.addIncludePath("C:\\Program Files\\");
                instance.addIncludePath("C:\\Program Files (x86)\\");
                instance.addIncludePath("C:\\Windows\\");
                instance.addIncludePath("C:\\green\\");
                String userHome = System.getProperty("user.home");
                instance.addIncludePath(userHome);
            } else {
                instance.addIncludePath("/bin/");
                instance.addIncludePath("/usr/bin");
                instance.addIncludePath("/usr/local/bin");
                instance.addIncludePath("/opt");
                String userHome = System.getProperty("user.home");
                instance.addIncludePath(userHome);
            }
        }


    }

    /**
     * 构建索引
     */
    private static void buildIndex() throws Exception {
        //建立索引
        HandlerPath handlerPath = HandlerPath.getInstance();
        System.out.println("Build Index Started...");
        long start = System.currentTimeMillis();
        String userHome = System.getProperty("user.home");
        Path indexPath = Paths.get(userHome, ".local","share", ".asu-run.idx");
        String[] includes = handlerPath.getIncludePath().toArray(new String[0]);
        String[] excludes = handlerPath.getExcludepath().toArray(new String[0]);
        IndexGenerator generator = new IndexGenerator(indexPath.toAbsolutePath().toString(), includes,excludes);
        generator.generateIndex();
        long end = System.currentTimeMillis();
        System.out.println("Build Index Complete. Cost " + (end - start) + " ms.");

    }
}
