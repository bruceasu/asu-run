package me.asu.run.index;

import java.io.File;
import me.asu.run.model.FileInfoConverter;
import me.asu.run.dao.FileIndexDao;
import me.asu.run.model.FileInfo;


/**
 * 将File转换为Thing然后写入数据库
 */
public class FileIndexInserter {

    private final FileIndexDao fileIndexDao;

    public FileIndexInserter(FileIndexDao fileIndexDao) {
        this.fileIndexDao = fileIndexDao;
    }

    /**
     * 打印
     * [ 转换、写入（Thing）]
     */
    public void apply(File file) {
        FileInfo fileInfo = FileInfoConverter.convert(file);
        if (!fileIndexDao.exists(file.getAbsolutePath())) {
            this.fileIndexDao.insert(fileInfo);
        } else {
            System.out.println(fileInfo.getPath() + " exists.");
        }
    }
}
