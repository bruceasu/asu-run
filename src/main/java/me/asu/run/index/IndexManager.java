package me.asu.run.index;


import java.io.File;
import java.util.List;
import me.asu.run.HandlerPath;
import me.asu.run.dao.FileIndexDao;
import me.asu.run.model.Condition;
import me.asu.run.model.FileInfo;

public class IndexManager {

    private static volatile IndexManager manager;

    /**
     * 业务层
     */

    private IndexSearch indexSearch;
    private FileMonitor fileMonitor;
    private FileIndexInserter fileIndexInserter;

    private IndexManager() {
        FileIndexDao fileIndexDao = new FileIndexDao();
        this.indexSearch = new IndexSearch(fileIndexDao);
        this.fileIndexInserter = new FileIndexInserter(fileIndexDao);
        this.fileMonitor = new FileMonitor(fileIndexDao);
    }

    public static IndexManager getInstance() {
        //double-check
        if (manager == null) {
            synchronized (IndexManager.class) {
                if (manager == null) {
                    manager = new IndexManager();
                }
            }
        }
        return manager;
    }


    /**
     * 检索功能
     */
    public List<FileInfo> search(Condition condition) {
        //Condition用户提供的是：name  file_Type
        condition.setLimit(Integer.MAX_VALUE);
        return this.indexSearch.search(condition);
    }

    public void insert(File file) {fileIndexInserter.apply(file);}

    /**
     * 文件监控
     */
    public void monitor() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                fileMonitor.monitor(HandlerPath.getInstance());
                fileMonitor.start();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void stopMonitor() {
        if (fileMonitor!=null) fileMonitor.stop();
    }
}
