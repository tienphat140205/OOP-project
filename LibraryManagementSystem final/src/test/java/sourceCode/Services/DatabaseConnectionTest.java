package sourceCode.Services;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {

    @Test
    public void testSingletonInstance() {
        DatabaseConnection instance1 = DatabaseConnection.getInstance();
        DatabaseConnection instance2 = DatabaseConnection.getInstance();
        assertSame(instance1, instance2, "DatabaseConnection should be a singleton");
    }

    @Test
    public void testDatabaseConnection() {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        try (Connection connection = dbConnection.getConnection()) {
            assertNotNull(connection, "Connection should not be null");
            assertFalse(connection.isClosed(), "Connection should be open");
        } catch (Exception e) {
            fail("Connection failed: " + e.getMessage());
        }
    }

    @Test
    public void testConnectionFailure() {
        String invalidPassword = "wrongPassword";
        DatabaseConnection instance = DatabaseConnection.getInstance();
        try {
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/library",
                    "root",
                    invalidPassword
            );
            fail("Connection should fail with incorrect credentials");
        } catch (Exception e) {
            assertInstanceOf(SQLException.class, e, "Expected SQLException on wrong credentials");
        }
    }
}