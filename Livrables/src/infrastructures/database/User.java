package infrastructures.database;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class User {

    private static final String TABLE_NAME = "user";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USER = "user";
    private static final String COLUMN_PASSWORD = "password";

    private DatabaseConnection databaseConnection;
    private static User instance;

    private User() throws SQLException {
        this.databaseConnection = DatabaseConnection.getInstance();
        initializeTable();
    }

    public static synchronized User getInstance() throws SQLException {
        if (instance == null) {
            instance = new User();
        }
        return instance;
    }

    /**
     * Initializes the user table if it does not exist
     */
    private void initializeTable() throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER + " TEXT NOT NULL UNIQUE, " +
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
     * Creates a new user entry (CREATE)
     *
     * @param user     the username
     * @param password the password for the user
     * @return the id of the created entry, or -1 if an error occurs
     * @throws SQLException if a database access error occurs
     */
    public int createUser(String user, String password) throws SQLException {
        String insertSQL = "INSERT INTO " + TABLE_NAME + " (" +
                COLUMN_USER + ", " +
                COLUMN_PASSWORD + ") VALUES (?, ?)";
        
        return createUserWithRetry(insertSQL, user, password, 0);
    }

    private int createUserWithRetry(String insertSQL, String user, String password, int attempt) throws SQLException {
        if (attempt > 3) {
            throw new SQLException("User creation failed: Timeout after multiple attempts");
        }

        try {
            Connection connection = databaseConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, user);
            preparedStatement.setString(2, password);

            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    return id;
                }
            }
            throw new SQLException("Creating user failed, no ID obtained.");
        } catch (SQLTimeoutException e) {
            return createUserWithRetry(insertSQL, user, password, attempt + 1);
        }
    }

    /**
     * Retrieves a user entry by its id (READ)
     *
     * @param id the entry id
     * @return a Map containing the entry data, or null if not found
     */
    public Map<String, Object> getUserById(int id) throws SQLException {
        String selectSQL = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ?";
        return getUserByIdWithRetry(selectSQL, id, 0);
    }

    private Map<String, Object> getUserByIdWithRetry(String selectSQL, int id, int attempt) throws SQLException {
        if (attempt > 3) {
            throw new SQLException("User retrieval failed: Timeout after multiple attempts");
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
            return getUserByIdWithRetry(selectSQL, id, attempt + 1);
        }
    }

    /**
     * Retrieves a user entry by username (READ)
     *
     * @param user the username
     * @return a Map containing the entry data, or null if not found
     */
    public Map<String, Object> getUserByUser(String user) throws SQLException {
        String selectSQL = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_USER + " = ?";
        return getUserByUserWithRetry(selectSQL, user, 0);
    }

    private Map<String, Object> getUserByUserWithRetry(String selectSQL, String user, int attempt) throws SQLException {
        if (attempt > 3) {
            throw new SQLException("User retrieval failed: Timeout after multiple attempts");
        }

        try {
            Connection connection = databaseConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(selectSQL);
            preparedStatement.setString(1, user);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return mapResultSetToMap(resultSet);
            }
            return null;
        } catch (SQLTimeoutException e) {
            return getUserByUserWithRetry(selectSQL, user, attempt + 1);
        }
    }

    /**
     * Checks if a username already exists (READ)
     *
     * @param user the username to check
     * @return true if the username exists, false otherwise
     */
    public boolean userExists(String user) throws SQLException {
        Map<String, Object> result = getUserByUser(user);
        return result != null;
    }

    /**
     * Updates a user entry (UPDATE)
     *
     * @param id       the id of the entry to update
     * @param user     the new username
     * @param password the new password
     * @return true if the update succeeded, false otherwise
     */
    public boolean updateUser(int id, String user, String password) {
        String updateSQL = "UPDATE " + TABLE_NAME + " SET " +
                COLUMN_USER + " = ?, " +
                COLUMN_PASSWORD + " = ? WHERE " + COLUMN_ID + " = ?";

        try {
            Connection connection = databaseConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(updateSQL);

            preparedStatement.setString(1, user);
            preparedStatement.setString(2, password);
            preparedStatement.setInt(3, id);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User entry updated with id: " + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating user entry: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a user entry (DELETE)
     *
     * @param id the id of the entry to delete
     * @return true if the deletion succeeded, false otherwise
     */
    public boolean deleteUser(int id) throws SQLException {
        String deleteSQL = "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ?";
        return deleteUserWithRetry(deleteSQL, id, 0);
    }

    private boolean deleteUserWithRetry(String deleteSQL, int id, int attempt) throws SQLException {
        if (attempt > 3) {
            throw new SQLException("User deletion failed: Timeout after multiple attempts");
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
            return deleteUserWithRetry(deleteSQL, id, attempt + 1);
        }
    }

    /**
     * Deletes a user entry by username (DELETE)
     *
     * @param user the username
     * @return true if the deletion succeeded, false otherwise
     */
    public boolean deleteUserByUser(String user) throws SQLException {
        String deleteSQL = "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_USER + " = ?";
        return deleteUserByUserWithRetry(deleteSQL, user, 0);
    }

    private boolean deleteUserByUserWithRetry(String deleteSQL, String user, int attempt) throws SQLException {
        if (attempt > 3) {
            throw new SQLException("User deletion failed: Timeout after multiple attempts");
        }

        try {
            Connection connection = databaseConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL);

            preparedStatement.setString(1, user);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                return true;
            }
            return false;
        } catch (SQLTimeoutException e) {
            return deleteUserByUserWithRetry(deleteSQL, user, attempt + 1);
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
        map.put(COLUMN_USER, resultSet.getString(COLUMN_USER));
        map.put(COLUMN_PASSWORD, resultSet.getString(COLUMN_PASSWORD));
        return map;
    }
}
