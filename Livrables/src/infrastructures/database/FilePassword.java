package infrastructures.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilePassword {

    private static final String TABLE_NAME = "file_password";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FILENAME = "filename";
    private static final String COLUMN_USER = "user";
    private static final String COLUMN_PASSWORD = "password";

    private DatabaseConnection databaseConnection;
    private static FilePassword instance;

    private FilePassword() throws SQLException {
        this.databaseConnection = DatabaseConnection.getInstance();
        initializeTable();
    }

    public static synchronized FilePassword getInstance() throws SQLException {
        if (instance == null) {
            instance = new FilePassword();
        }
        return instance;
    }

    /**
     * Initializes the file_password table if it does not exist
     */
    private void initializeTable() throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FILENAME + " TEXT NOT NULL, " +
                COLUMN_USER + " TEXT NOT NULL, " +
                COLUMN_PASSWORD + " TEXT NOT NULL" +
                ")";

        initializeTableWithRetry(createTableSQL, 0);
    }

    private void initializeTableWithRetry(String createTableSQL, int attempt) throws SQLException {
        if (attempt > 3) {
            throw new SQLException("Table initialization failed: Timeout after multiple attempts");
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
     * Creates a new file password entry (CREATE)
     *
     * @param filename the filename
     * @param user     the user who owns the password
     * @param password the password for the file
     * @return the id of the created entry, or -1 if an error occurs
     * @throws SQLException if a database access error occurs
     */
    public int createFilePassword(String filename, String user, String password) throws SQLException {
        String insertSQL = "INSERT INTO " + TABLE_NAME + " (" +
                COLUMN_FILENAME + ", " +
                COLUMN_USER + ", " +
                COLUMN_PASSWORD + ") VALUES (?, ?, ?)";
        
        return createFilePasswordWithRetry(insertSQL, filename, user, password, 0);
    }

    private int createFilePasswordWithRetry(String insertSQL, String filename, String user, String password, int attempt) throws SQLException {
        if (attempt > 3) {
            throw new SQLException("File password creation failed: Timeout after multiple attempts");
        }

        try {
            Connection connection = databaseConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, filename);
            preparedStatement.setString(2, user);
            preparedStatement.setString(3, password);

            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    return id;
                }
            }
            throw new SQLException("Creating file password failed, no ID obtained.");
        } catch (SQLTimeoutException e) {
            return createFilePasswordWithRetry(insertSQL, filename, user, password, attempt + 1);
        }
    }

    /**
     * Retrieves a file password entry by its id (READ)
     *
     * @param id the entry id
     * @return a Map containing the entry data, or null if not found
     */
    public Map<String, Object> getFilePasswordById(int id) throws SQLException {
        String selectSQL = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ?";
        return getFilePasswordByIdWithRetry(selectSQL, id, 0);
    }

    private Map<String, Object> getFilePasswordByIdWithRetry(String selectSQL, int id, int attempt) throws SQLException {
        if (attempt > 3) {
            throw new SQLException("File password retrieval failed: Timeout after multiple attempts");
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
            return getFilePasswordByIdWithRetry(selectSQL, id, attempt + 1);
        }
    }

    /**
     * Retrieves a file password entry by filename (READ)
     *
     * @param filename the filename
     * @return a Map containing the entry data, or null if not found
     */
    public Map<String, Object> getFilePasswordByFilename(String filename) throws SQLException {
        String selectSQL = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_FILENAME + " = ?";
        return getFilePasswordByFilenameWithRetry(selectSQL, filename, 0);
    }

    private Map<String, Object> getFilePasswordByFilenameWithRetry(String selectSQL, String filename, int attempt) throws SQLException {
        if (attempt > 3) {
            throw new SQLException("File password retrieval failed: Timeout after multiple attempts");
        }

        try {
            Connection connection = databaseConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(selectSQL);
            preparedStatement.setString(1, filename);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return mapResultSetToMap(resultSet);
            }
            return null;
        } catch (SQLTimeoutException e) {
            return getFilePasswordByFilenameWithRetry(selectSQL, filename, attempt + 1);
        }
    }

    /**
     * Updates a file password entry (UPDATE)
     *
     * @param id       the id of the entry to update
     * @param filename the new filename
     * @param user     the new user
     * @param password the new password
     * @return true if the update succeeded, false otherwise
     */
    public boolean updateFilePassword(int id, String filename, String user, String password) {
        String updateSQL = "UPDATE " + TABLE_NAME + " SET " +
                COLUMN_FILENAME + " = ?, " +
                COLUMN_USER + " = ?, " +
                COLUMN_PASSWORD + " = ? WHERE " + COLUMN_ID + " = ?";

        try {
            Connection connection = databaseConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(updateSQL);

            preparedStatement.setString(1, filename);
            preparedStatement.setString(2, user);
            preparedStatement.setString(3, password);
            preparedStatement.setInt(4, id);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("File password entry updated with id: " + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating file password entry: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a file password entry (DELETE)
     *
     * @param id the id of the entry to delete
     * @return true if the deletion succeeded, false otherwise
     */
    public boolean deleteFilePassword(int id) throws SQLException {
        String deleteSQL = "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ?";
        return deleteFilePasswordWithRetry(deleteSQL, id, 0);
    }

    private boolean deleteFilePasswordWithRetry(String deleteSQL, int id, int attempt) throws SQLException {
        if (attempt > 3) {
            throw new SQLException("File password deletion failed: Timeout after multiple attempts");
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
            return deleteFilePasswordWithRetry(deleteSQL, id, attempt + 1);
        }
    }

    /**
     * Deletes a file password entry by filename (DELETE)
     *
     * @param filename the filename
     * @return true if the deletion succeeded, false otherwise
     */
    public boolean deleteFilePasswordByFilename(String filename) throws SQLException {
        String deleteSQL = "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_FILENAME + " = ?";
        return deleteFilePasswordByFilenameWithRetry(deleteSQL, filename, 0);
    }

    private boolean deleteFilePasswordByFilenameWithRetry(String deleteSQL, String filename, int attempt) throws SQLException {
        if (attempt > 3) {
            throw new SQLException("File password deletion failed: Timeout after multiple attempts");
        }

        try {
            Connection connection = databaseConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL);

            preparedStatement.setString(1, filename);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                return true;
            }
            return false;
        } catch (SQLTimeoutException e) {
            return deleteFilePasswordByFilenameWithRetry(deleteSQL, filename, attempt + 1);
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
        map.put(COLUMN_FILENAME, resultSet.getString(COLUMN_FILENAME));
        map.put(COLUMN_USER, resultSet.getString(COLUMN_USER));
        map.put(COLUMN_PASSWORD, resultSet.getString(COLUMN_PASSWORD));
        return map;
    }
}
