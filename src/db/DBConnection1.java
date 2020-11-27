package db;

import java.sql.*;

public class DBConnection1 {

    private static DBConnection1 dbConnection;
    private Connection connection;

    private DBConnection1() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/JDBC2", "root", "mysql");
        }catch (SQLSyntaxErrorException e){
            try {
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306","root","mysql");
                String sql = "CREATE DATABASE JDBC2";
                PreparedStatement pstm = connection.prepareStatement(sql);
                pstm.execute();
                connection.close();
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/JDBC2", "root", "mysql");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static DBConnection1 getInstance() {
        return (dbConnection == null) ? (dbConnection = new DBConnection1()) : dbConnection;
    }

    public Connection getConnection() {
        return connection;
    }

}
