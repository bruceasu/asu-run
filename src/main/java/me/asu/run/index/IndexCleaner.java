package me.asu.run.index;

import me.asu.run.model.FileInfo;

import java.util.Queue;
import me.asu.run.dao.FileIndexDao;

public class IndexCleaner implements Runnable {

    private final FileIndexDao fileIndexDao;

    private final Queue<FileInfo> fileInfoQueue;

    public IndexCleaner(FileIndexDao fileIndexDao, Queue<FileInfo> fileInfoQueue) {
        this.fileIndexDao  = fileIndexDao;
        this.fileInfoQueue = fileInfoQueue;
    }

    public void apply(FileInfo fileInfo) {
        this.fileIndexDao.delete(fileInfo.getPath());
    }

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            FileInfo fileInfo =  this.fileInfoQueue.poll();
            if(fileInfo != null){
                this.apply(fileInfo);
            }
        }
    }
}
