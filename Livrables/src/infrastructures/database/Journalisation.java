package infrastructures.database;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Journalisation {

    private static final String TABLE_NAME = "journalisation";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USER = "user";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_ACTION_TYPE = "action_type";
    private static final String COLUMN_FILE = "file";

    private DatabaseConnection databaseConnection;
    private static Journalisation instance;

    private Journalisation() {
        this.databaseConnection = DatabaseConnection.getInstance();
        initializeTable();
    }

    public static synchronized Journalisation getInstance() {
        if (instance == null) {
            instance = new Journalisation();
        }
        return instance;
    }

    /**
     * Initializes the journalisation table if it does not exist
     */
    private void initializeTable() throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER + " TEXT NOT NULL, " +
                COLUMN_DATE + " DATETIME NOT NULL, " +
                COLUMN_ACTION_TYPE + " TEXT NOT NULL, " +
                COLUMN_FILE + " TEXT NOT NULL" +
                ")";

        initializeTableWithRetry(createTableSQL, 0);
        }

        private void initializeTableWithRetry(String createTableSQL, int attempt) throws SQLException {
        if (attempt > 3) {
            throws new SQLException("Table initialization failed: Timeout after multiple attempts");
        }

        try {
            Connection connection = databaseConnection.getConnection();
            Statement statement = connection.createStatement();
            statement.execute(createTableSQL);
        } catch (SQLTimeoutException e) {
            initializeTableWithRetry(createTableSQL, attempt + 1);
        }
    }

    /**
     * Creates a new journalisation entry (CREATE)
     *
     * @param actionId   the action id
     * @param user       the user who performed the action
     * @param actionType the type of action (CREATE, READ, UPDATE, DELETE, etc.)
     * @param file       the path of the affected file
     * @return the id of the created entry, or -1 if an error occurs
     */
    public int createLog(int actionId, String user, String actionType, String file) throws SQLException {
        String insertSQL = "INSERT INTO " + TABLE_NAME + " (" +
                COLUMN_USER + ", " +
                COLUMN_DATE + ", " +
                COLUMN_ACTION_TYPE + ", " +
                COLUMN_FILE + ") VALUES (?, ?, ?, ?, ?)";
        
        return createLogWithRetry(insertSQL, actionId, user, actionType, file, 0);
    }

    private int createLogWithRetry(String insertSQL, int actionId, String user, String actionType, String file, int attempt) throws SQLException {
        if (attempt > 3) {
            throw new SQLException("Log creation failed: Timeout after multiple attempts");
        }

        try {
            Connection connection = databaseConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, user);
            preparedStatement.setString(2, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            preparedStatement.setString(3, actionType);
            preparedStatement.setString(4, file);

            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    return id;
                }
            }
        } catch (SQLTimeoutException e) {
            return createLogWithRetry(insertSQL, actionId, user, actionType, file, attempt + 1);
        }
    }

    /**
     * Retrieves a journalisation entry by its id (READ)
     *
     * @param id the entry id
     * @return a Map containing the entry data, or null if not found
     */
    public Map<String, Object> getLogById(int id) throws SQLException {
        String selectSQL = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ?";
        return getLogByIdWithRetry(selectSQL, id, 0);
    }

    private Map<String, Object> getLogByIdWithRetry(String selectSQL, int id, int attempt) throws SQLException {
        if (attempt > 3) {
            throw new SQLException("Log retrieval failed: Timeout after multiple attempts");
        }

        try {
            Connection connection = databaseConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(selectSQL);
            preparedStatement.setInt(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return mapResultSetToMap(resultSet);
            }
            return null;
        } catch (SQLTimeoutException e) {
            return getLogByIdWithRetry(selectSQL, id, attempt + 1);
        }
    }
    }

    /**
     * Updates a journalisation entry (UPDATE)
     *
     * @param id         the id of the entry to update
     * @param user       the new user
     * @param actionType the new action type
     * @param file       the new file
     * @return true if the update succeeded, false otherwise
     */
    public boolean updateLog(int id, String user, String actionType, String file) {
        String updateSQL = "UPDATE " + TABLE_NAME + " SET " +
                COLUMN_USER + " = ?, " +
                COLUMN_ACTION_TYPE + " = ?, " +
                COLUMN_FILE + " = ?, " +
                COLUMN_DATE + " = ? WHERE " + COLUMN_ID + " = ?";

        try {
            Connection connection = databaseConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(updateSQL);

            preparedStatement.setString(1, user);
            preparedStatement.setString(2, actionType);
            preparedStatement.setString(3, file);
            preparedStatement.setString(4, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            preparedStatement.setInt(5, id);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Journalisation entry updated with id: " + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating journalisation entry: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a journalisation entry (DELETE)
     *
     * @param id the id of the entry to delete
     * @return true if the deletion succeeded, false otherwise
     */
    public boolean deleteLog(int id) throws SQLException {
        String deleteSQL = "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ?";
        return deleteLogWithRetry(deleteSQL, id, 0);
    }

    private boolean deleteLogWithRetry(String deleteSQL, int id, int attempt) throws SQLException {
        if (attempt > 3) {
            throw new SQLException("Log deletion failed: Timeout after multiple attempts");
        }

        try {
            Connection connection = databaseConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL);

            preparedStatement.setInt(1, id);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                return true;
            }
            return false;
        } catch (SQLTimeoutException e) {
            return deleteLogWithRetry(deleteSQL, id, attempt + 1);
        }
    }

    /**
     * Converts a ResultSet to a Map
     *
     * @param resultSet the ResultSet to convert
     * @return a Map containing the ResultSet data
     * @throws SQLException if an error occurs
     */
    private Map<String, Object> mapResultSetToMap(ResultSet resultSet) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        map.put(COLUMN_ID, resultSet.getInt(COLUMN_ID));
        map.put(COLUMN_ACTION_ID, resultSet.getInt(COLUMN_ACTION_ID));
        map.put(COLUMN_USER, resultSet.getString(COLUMN_USER));
        map.put(COLUMN_DATE, resultSet.getString(COLUMN_DATE));
        map.put(COLUMN_ACTION_TYPE, resultSet.getString(COLUMN_ACTION_TYPE));
        map.put(COLUMN_FILE, resultSet.getString(COLUMN_FILE));
        return map;
    }
}