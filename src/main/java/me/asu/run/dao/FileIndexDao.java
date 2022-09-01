package me.asu.run.dao;


import static me.asu.run.SimilarTextUtils.getSimilarity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import me.asu.run.model.Condition;
import me.asu.run.model.FileInfo;
import me.asu.run.model.FileType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FileIndexDao {
    public void insert(FileInfo fileInfo) {
        //JDBC操作
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DataSourceFactory.getConnection();
            String sql = "insert into fileInfo(name,path,file_type,file_ext) values(?,?,?,?)";
            statement = connection.prepareStatement(sql);
            //预编译命令中SQL的占位符赋值
            statement.setString(1, fileInfo.getName());
            statement.setString(2, fileInfo.getPath());
            statement.setString(3, fileInfo.getFileType().name());
            statement.setString(4, fileInfo.getExt());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            releaseResource(null, statement, connection);
        }
    }

    /**
     * thing -> path => D:\a\b\hello.java
     * thing -> path => D:\a\b
     *                  D:\a\ba
     * like => dir%
     */
    public void deleteDir(String dir) {
        if (dir == null || dir.trim().isEmpty()) return;
        dir = dir.trim().replaceAll("'","''");
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DataSourceFactory.getConnection();
            String sql = "delete from fileInfo where path like '"+dir+"%'";
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            releaseResource(null, statement, connection);
        }
    }
    /**
     * 绝对匹配
     */
    public void delete(String path) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DataSourceFactory.getConnection();
            String sql = "delete from fileInfo where path = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, path);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            releaseResource(null, statement, connection);
        }
    }

    public int deleteByExt(String ext) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DataSourceFactory.getConnection();
            String sql = "delete from fileInfo where file_ext = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, ext);
            return statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            releaseResource(null, statement, connection);
        }
    }

    public List<FileInfo> query(Condition condition) {
        List<FileInfo> fileInfos = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("select name,path,file_type,file_ext from fileInfo");
            sb.append(" where ");
            //采用模糊匹配
            //前迷糊
            //后模糊
            //前后模糊
            sb.append("locate (lower(?), name_lower)");
           List<String> params = new ArrayList<>(3);
           params.add(condition.getName());
            if (condition.getFileType() != null) {
                FileType fileType = FileType.lookupByName(condition.getFileType());
                sb.append(" and file_type=?");
                params.add(fileType.name());
            }
            if (condition.getFileExt() != null) {
                sb.append(" and file_ext=?");
                params.add(condition.getFileExt());
            }
            sb.append(" order by name");
            sb.append(" limit ").append(condition.getLimit());
//            System.out.println("SQL: " + sb.toString());
            connection = DataSourceFactory.getConnection();
            statement = connection.prepareStatement(sb.toString());
            for (int i = 0; i < params.size(); i++) {
                statement.setString(i+1, params.get(i));
            }

            resultSet = statement.executeQuery();
            //处理结果
            while (resultSet.next()) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setName(resultSet.getString("name"));
                fileInfo.setPath(resultSet.getString("path"));
                fileInfo.setFileType(FileType.lookupByName(resultSet.getString("file_type")));
                fileInfo.setExt(resultSet.getString("file_ext"));
                fileInfo.setScore(getSimilarity( condition.getName(), fileInfo.getName()));
                fileInfos.add(fileInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            releaseResource(resultSet, statement, connection);
        }
        Collections.sort(fileInfos);
//        for (FileInfo fi : fileInfos) {
//            System.out.println(fi.getScore() + "\t" + fi.getName());
//        }
        return fileInfos;
    }

    public boolean exists(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        path = path.replaceAll("\'", "\'\'");
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("select count(1) from fileInfo");
            sb.append(" where ");
            //采用模糊匹配
            //前迷糊
            //后模糊
            //前后模糊
            sb.append(" path = '").append(path).append("'");
            connection = DataSourceFactory.getConnection();
            statement = connection.prepareStatement(sb.toString());
            resultSet = statement.executeQuery();
            //处理结果
            if (resultSet.next()) {
                int anInt = resultSet.getInt(1);
                return anInt == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            releaseResource(resultSet, statement, connection);
        }
        return false;
    }

    /**
     * 全库索引更新
     */
    public void cleanNotExists() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            String sql = "select path from fileInfo";
            connection = DataSourceFactory.getConnection();
            statement = connection.prepareStatement(sql);
                  resultSet = statement.executeQuery();
            //处理结果
            while (resultSet.next()) {
                String path = resultSet.getString(1);
                Path p = Paths.get(path);
                if (!Files.isRegularFile(p)) {
                    delete(path);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            releaseResource(resultSet, statement, connection);
        }
    }

    /**
     * 全库有多个相同文件时，清除多个，
     * 通常是由于windows系统不区分大小写导致的
     */
    public void removeDupIndex() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            String sql = "select path from fileInfo";
            connection = DataSourceFactory.getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            //处理结果
            Map<String, List<String>> m = new HashMap<>();
            while (resultSet.next()) {
                String path = resultSet.getString(1);
                if (path.length() >= 2 && path.charAt(1) == ':') {
                    // windows file
                    String name = path.toLowerCase();
                    m.putIfAbsent(name, new LinkedList<>());
                    m.get(name).add(path);
                }
            }
            m.forEach((n, ps)->{
                if (ps.size() == 1) return;
                for (int i = 1; i < ps.size(); i++) {
                    String s = ps.get(i);
                    delete(s);
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            releaseResource(resultSet, statement, connection);
        }
    }

    /**
     * 重构
     * 在不改变程序的功能和业务的前提下，对代码进行优化，是的代码更易阅读和扩展
     */
    private void releaseResource(ResultSet resultSet,
            PreparedStatement statement,
            Connection connection) {
        DataSourceFactory.closeQuietly(resultSet);
        DataSourceFactory.closeQuietly(statement);
        DataSourceFactory.closeQuietly(connection);
    }

}
