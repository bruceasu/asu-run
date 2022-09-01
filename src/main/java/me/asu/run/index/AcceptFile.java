package me.asu.run.index;

import me.asu.run.model.FileInfo;
import me.asu.run.model.FileType;

public class AcceptFile {

    boolean accept(FileInfo f) {
        FileType t = f.getFileType();
        return t == FileType.BIN
                || t == FileType.WIN_SCRIPT
                || t == FileType.SHORTCUT;
    }
}
