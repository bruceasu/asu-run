package me.asu.run;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件对象转换Thing对象的辅助类
 */

public class FileInfoConverter {

    public static FileInfo convert(File file) {
        FileInfo fileInfo = new FileInfo();
        String name = file.getName();
        fileInfo.setName(name);
        fileInfo.setPath(file.getAbsolutePath());
        /**
         * 目录 -> *
         * 文件 -> 有扩展名，通过扩展名获取FileType
         *         无扩展，*
         */
        int index = name.lastIndexOf(".");
        String extend = "*";
        if (index != -1 && (index + 1) < name.length()) {
            extend = name.substring(index + 1);
        }
        fileInfo.setFileType(FileType.lookupByExtend(extend));
        fileInfo.setExt(extend);
        return fileInfo;
    }

    public static FileInfo convert(String file) {
        return convert(Paths.get(file));
    }

    public static FileInfo convert(Path file) {
        FileInfo fileInfo = new FileInfo();
        String name = file.getFileName().toString();
        fileInfo.setName(name);
        fileInfo.setPath(file.toAbsolutePath().toString());
        /**
         * 目录 -> *
         * 文件 -> 有扩展名，通过扩展名获取FileType
         *         无扩展，*
         */
        int index = name.lastIndexOf(".");
        String extend = "*";
        if (index != -1 && (index + 1) < name.length()) {
            extend = name.substring(index + 1);
        }
        fileInfo.setFileType(FileType.lookupByExtend(extend));
        fileInfo.setExt(extend);
        return fileInfo;
    }
}
