package me.asu.run;

public class AcceptFile {

    public static boolean accept(FileInfo f) {
        FileType t = f.getFileType();
        return t == FileType.BIN
                || t == FileType.WIN_SCRIPT
                || t == FileType.SHORTCUT;
    }
}
