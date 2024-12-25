package sourceCode.Services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static volatile DatabaseConnection instance;

    private DatabaseConnection() {

    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        String PASSWORD = "Ntp2716032814@"; // Change this to your MySQL password
        String USER = "root";
        String URL = "jdbc:mysql://localhost:3306/library";
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
