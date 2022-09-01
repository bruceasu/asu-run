package me.asu.run.index;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import me.asu.run.HandlerPath;
import me.asu.run.model.FileInfo;
import me.asu.run.model.FileInfoConverter;

public class FileIndexScan {

    private HandlerPath handlerPath = HandlerPath.getInstance();

    private              IndexManager    indexManager    = IndexManager.getInstance();

    public void index(String ...paths) {
        AcceptFile af = new AcceptFile();
        Set<String> excludePaths = handlerPath.getExcludepath();
        LinkedList<String> queue = new LinkedList<>();
        queue.addAll(Arrays.asList(paths));
        List<String> plainExclude = new ArrayList<>();
        List<Pattern> regexExclude = new ArrayList<>();
        for (String excludePath : excludePaths) {
            if (excludePath.contains("*") ||excludePath.contains("?") ) {
                regexExclude.add(Pattern.compile(excludePath));
            } else {
                plainExclude.add(excludePath);
            }
        }
        next:
        while (queue.size() > 0) {
            String s = queue.pop();

            for (String excludePath : plainExclude) {
                if (s.startsWith(excludePath)) continue next;
            }
            for (Pattern p : regexExclude) {
                if(p.matcher(s).find()) continue next;
            }

            File file = new File(s);
            if (file.isDirectory()) {
                System.out.println("scan path: " + s);
                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isDirectory()) {
                            queue.push(f.getAbsolutePath());
                        } else {
                            addIndex(af, f);
                        }
                    }
                }
            } else {
                addIndex(af, file);
            }
        }
    }

    private void addIndex(AcceptFile af, File file) {
        FileInfo fi = FileInfoConverter.convert(file);
        if (af.accept(fi)) {
            System.out.println("Index: " + fi);
            indexManager.insert(file);
        }
    }

}
