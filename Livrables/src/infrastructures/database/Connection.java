import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

package infrastructures.database;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private final String DATABASE_URL = "jdbc:sqlite:database.db";

    private DatabaseConnection() {
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DATABASE_URL);
        }
        return connection;
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}