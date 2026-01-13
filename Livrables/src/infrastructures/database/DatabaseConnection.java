package infrastructures.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    private static final String DATABASE_NAME = "db_challenge_technique.db";
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_NAME;

    private DatabaseConnection() throws SQLException {
        // créer une table pour tester la connexion
        getConnection().createStatement().execute("CREATE TABLE IF NOT EXISTS test_table (id INTEGER PRIMARY KEY)");
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
            System.out.println("Connexion SQLite établie (base créée si absente)");
        }
        return connection;
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Connexion SQLite fermée");
        }
    }
}
