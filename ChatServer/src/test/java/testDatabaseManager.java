import cn.lokilanka.utils.DatabaseManager;

import java.sql.SQLException;

public class testDatabaseManager {
    private static DatabaseManager databaseManager = null;

    static {
        try {
            databaseManager = new DatabaseManager("user_table.db");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public testDatabaseManager() throws SQLException, ClassNotFoundException {
    }
    public static void main(String[] args) throws SQLException {
        databaseManager.selectUser("asd");
        databaseManager.createUser("ddd");
    }
}
