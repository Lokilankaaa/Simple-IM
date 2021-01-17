package cn.lokilanka.utils;

import java.sql.*;

public class DatabaseManager {
    private final Connection connection;
    Statement statement;


    public DatabaseManager(String path) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        statement = connection.createStatement();
    }

    public boolean selectUser(String name) throws SQLException {
        String sql = "select * from user_table where name=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, name);
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.next();
    }

    public boolean createUser(String name) throws SQLException {
        if (selectUser(name))
            return false;
        String sql = "insert into user_table (name, date)" + "values(?, CURRENT_DATE)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();
            return true;
        }
    }
}
