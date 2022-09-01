package me.asu.run.index;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import me.asu.run.dao.FileIndexDao;
import me.asu.run.model.Condition;
import me.asu.run.model.FileInfo;

public class IndexSearch {

    private final FileIndexDao fileIndexDao;

    private final IndexCleaner interceptor;

    private final Queue<FileInfo> fileInfoQueue = new ArrayBlockingQueue<>(1024);

    public IndexSearch(FileIndexDao fileIndexDao) {
        this.fileIndexDao = fileIndexDao;
        this.interceptor  = new IndexCleaner(this.fileIndexDao, fileInfoQueue);
        this.backgroundClearThread();
    }

    public List<FileInfo> search(Condition condition) {
        // 此时如果查询结果存在已经在文件系统中删除的文件，
        // 那么需要在数据库中清除掉该文件的索引信息
        List<FileInfo> fileInfos = this.fileIndexDao.query(condition);
        Iterator<FileInfo> iterator = fileInfos.iterator();
        while (iterator.hasNext()) {
            FileInfo fileInfo = iterator.next();
            File file = new File(fileInfo.getPath());
            if (!file.exists()) {
                //删除
                iterator.remove();
                this.fileInfoQueue.add(fileInfo);
            }
        }
        return fileInfos;
    }


    public void backgroundClearThread() {
        //进行后台清理工作
        Thread thread = new Thread(this.interceptor);
        thread.setName("Thread-Clear");
        thread.setDaemon(true);
        thread.start();
    }

}
