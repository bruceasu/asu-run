package me.asu.run.model;




import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public enum FileType {
    IMG("jpg","jpeg","png","bmp","gif","svg","ico", "heic","tga", "tif", "tiff", "wbmp", "webp", "xbm", "xpm","psd","psb"),
    DOC("doc","docx","pdf","ppt","pptx","xls","txt","html", "htm","org", "md"),
    BIN("exe","msi"),
    SCRIPT("sh","py","pl", "rb"),
    WIN_SCRIPT("bat", "cmd"),
    SHORTCUT( "lnk"),
    JAVA("jar"),
    ARCHIVE("zip","rar","7z","gz","tar.gz", "tgz", "xz"),
    OTHER("*");

    private Set<String> extend = new HashSet<>();

    FileType(String...extend){
        this.extend.addAll(Arrays.asList(extend));
    }

    public static FileType lookupByExtend(String extend){
        for(FileType fileType:FileType.values()){
            if(fileType.extend.contains(extend.toLowerCase())){
                return fileType;
            }
        }
        return FileType.OTHER;
    }

    public static FileType lookupByName(String name){
        for(FileType fileType:FileType.values()){
            if(fileType.name().equals(name)){
                return fileType;
            }
        }
        return FileType.OTHER;
    }
}
