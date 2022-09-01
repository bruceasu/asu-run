package me.asu.run.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataSourceFactory {

    static {
        databaseInit();
    }

    public static Connection getConnection() throws SQLException {
        String driver = "org.h2.Driver";
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//        } catch (ClassNotFoundException e) {
//            // ignore
//        }
//        try {
//            Class.forName("org.postgresql.Driver");
//        } catch (ClassNotFoundException e) {
//            // ignore
//        }
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        String path = System.getProperty("user.home") + File.separator
                + ".asu-run";
        String jdbcUrl = "jdbc:h2:" + path;
        String username = "";
        String password = "";
        final Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

        return connection;

    }

    private static void databaseInit() {
        //classpath:databases.sql => String
        StringBuilder sb = new StringBuilder();
        try (InputStream in = DataSourceFactory.class.getClassLoader()
                                                     .getResourceAsStream("database.sql");

        ) {
            if (in != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                throw new RuntimeException("database.sql script can't load please check it.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String sql = sb.toString();
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            statement  = connection.prepareStatement(sql);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(statement);
            closeQuietly(connection);
        }
    }

    public static void closeQuietly(AutoCloseable a) {
        if (a != null) { return; }
        try {
            a.close();
        } catch (Exception e) {
            //ignored
        }
    }

}

