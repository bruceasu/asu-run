package me.asu.run;

import lombok.Data;

@Data
public class FileInfo implements Comparable<FileInfo> {
    /*文件名称*/
    private String name;
    /*文件路径*/
    private String path;
    /*文件类型*/
    private FileType fileType;
    private String ext;

    private transient double score;

    public int compareTo(FileInfo o) {
        double c = o.score - score;
        if (c > 0) {
            return 1;
        } else if (c < 0) {
            return -1;
        } else {
            return 0;
        }
    }
}

