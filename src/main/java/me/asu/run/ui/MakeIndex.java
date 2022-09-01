package me.asu.run.ui;

import java.io.File;
import java.util.Set;
import me.asu.run.HandlerPath;
import me.asu.run.OsUtils;
import me.asu.run.index.FileIndexScan;

public class MakeIndex {



    public static void main(String[] args) {
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
        if (instance.getIncludePath().isEmpty()) {
            if (OsUtils.WINDOWS) {
                File[] roots = File.listRoots();
                for (int i = 0; i < roots.length; i++) {
                    // 磁盘路径
                    instance.addIncludePath(roots[i].getPath());
                }
            } else {
                instance.addIncludePath("/");
            }
        }

        instance.addExcludePath(".*\\.git$");
        instance.addExcludePath(".*\\.cvs$");
        instance.addExcludePath(".*\\.svn$");
        instance.addExcludePath(".*\\.settings$");
        instance.addExcludePath(".*\\.idea$");
        instance.addExcludePath(".*\\node_modules$");
        instance.addExcludePath(".*\\.m2$");
        instance.addExcludePath(".*RECYCLE.BIN.*");
        instance.addExcludePath(".*\\.Trash-\\d+$");
        instance.addExcludePath(".+:\\\\Users\\\\.*\\\\AppData\\\\Roaming\\\\Microsoft\\\\Windows\\\\Recent$");

    }

    /**
     * 构建索引
     */
    private static void buildIndex() {
        FileIndexScan fileIndexScan = new FileIndexScan();
        //建立索引
        HandlerPath handlerPath = HandlerPath.getInstance();
        Set<String> includePaths = handlerPath.getIncludePath();
        System.out.println("Build Index Started...");
        long start = System.currentTimeMillis();
        fileIndexScan.index(includePaths.toArray(new String[includePaths.size()]));
        long end = System.currentTimeMillis();
        System.out.println(
                "Build Index Complete. Cost " + (end - start) + " ms.");

    }
}
