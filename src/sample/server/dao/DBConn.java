package sample.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
//import java.util.ResourceBundle;

public class DBConn {
    private static final DBConn INSTANCE = new DBConn();
    private Connection conn;
    private DBConn(){
        try {
            Class.forName("org.sqlite.JDBC");
            String jdbcURL = "jdbc:sqlite:src/rss/chat.db";
            this.conn = DriverManager.getConnection(jdbcURL);
            System.out.println("Подключение к базе данных установлено.");
        } catch (SQLException | ClassNotFoundException e){
            e.getMessage();
            e.printStackTrace();
        }

    }

    public static DBConn getInstance(){
        return INSTANCE;
    }

    public Connection getConn(){
        return this.conn;
    }

}
