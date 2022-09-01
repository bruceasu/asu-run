package me.asu.run.model;

import java.io.File;

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
        int index = file.getName().lastIndexOf(".");
        String extend = "*";
        if (index != -1 && (index + 1) < name.length()) {
            extend = name.substring(index + 1);
        }
        fileInfo.setFileType(FileType.lookupByExtend(extend));
        fileInfo.setExt(extend);
        return fileInfo;
    }

}
