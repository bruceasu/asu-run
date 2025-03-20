package me.asu.run;

import java.util.List;

public interface SearchEngine {
    List<FileInfo> search(Condition condition);
}
