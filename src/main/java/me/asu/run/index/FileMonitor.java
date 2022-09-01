package me.asu.run.index;

import java.io.File;
import java.util.Set;
import me.asu.run.HandlerPath;
import me.asu.run.dao.FileIndexDao;
import me.asu.run.model.FileInfo;
import me.asu.run.model.FileInfoConverter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

public class FileMonitor extends FileAlterationListenerAdaptor {

    private final FileAlterationMonitor monitor;
    private final AcceptFile af = new AcceptFile();
    private final FileIndexDao fileIndexDao;

    public FileMonitor(FileIndexDao fileIndexDao) {
        this.monitor      = new FileAlterationMonitor(600000);
        this.fileIndexDao = fileIndexDao;
    }

    public void start() {
        //启动
        try {
            this.monitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void monitor(HandlerPath handlerPath) {
        //监控的目录
        Set<String> includePath = handlerPath.getIncludePath();
        for (String path : includePath) {
            FileAlterationObserver observer = new FileAlterationObserver(new File(path), pathname -> {
                for (String exclude : handlerPath.getExcludepath()) {
                    if (pathname.getAbsolutePath().startsWith(exclude)) {
                        return false;
                    }
                }
                return true;
            });
            observer.addListener(this);
            this.monitor.addObserver(observer);
        }
    }

    @Override
    public void onDirectoryCreate(File directory) {
    }

    @Override
    public void onDirectoryDelete(File directory) {
        this.fileIndexDao.deleteDir(directory.getAbsolutePath());
    }

    @Override
    public void onFileCreate(File file) {
        FileInfo fi = FileInfoConverter.convert(file);
        if (af.accept(fi)) {
            System.out.println("Index: " + fi);
            if (!fileIndexDao.exists(file.getAbsolutePath()))
                this.fileIndexDao.insert(fi);
        }
    }

    @Override
    public void onFileDelete(File file) {
        this.fileIndexDao.delete(file.getAbsolutePath());
    }

    public void stop() {
        //停止
        try {
            this.monitor.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
