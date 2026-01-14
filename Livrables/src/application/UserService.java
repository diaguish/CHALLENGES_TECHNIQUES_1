package application;

import infrastructures.database.User;
import infrastructures.security.CryptoService;
import java.sql.SQLException;
import java.util.Map;

public class UserService {

    private User userDatabase;
    private static UserService instance;
    private String currentUser;

    private UserService() throws SQLException {
        this.userDatabase = User.getInstance();
        this.currentUser = null;
    }

    public static synchronized UserService getInstance() throws SQLException {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    /**
     * Verifies a password against a hash and salt
     * @param password the plain password to verify
     * @param hashedPassword the stored hashed password
     * @param salt the salt used for hashing
     * @return true if the password matches
     */
    private boolean verifyPassword(String password, String hashedPassword, String salt) {
        String computedHash = CryptoService.hashPassword(password, salt);
        return computedHash.equals(hashedPassword);
    }

    /**
     * Registers a new user
     *
     * @param username the username
     * @param password the password
     * @return a message indicating success or failure
     */
    public String register(String username, String password) {
        try {
            // Check if username already exists
            if (userDatabase.userExists(username)) {
                return "Erreur: Le nom d'utilisateur '" + username + "' est déjà utilisé.";
            }

            // Validate input
            if (username == null || username.trim().isEmpty()) {
                return "Erreur: Le nom d'utilisateur ne peut pas être vide.";
            }
            if (password == null || password.trim().isEmpty()) {
                return "Erreur: Le mot de passe ne peut pas être vide.";
            }

            // Create the user
            String salt = CryptoService.generateSalt();
            String hashedPassword = CryptoService.hashPassword(password, salt);
            int userId = userDatabase.createUser(username, hashedPassword, salt);
            if (userId > 0) {
                return "Utilisateur '" + username + "' créé avec succès.";
            } else {
                return "Erreur: Impossible de créer l'utilisateur.";
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
            return "Erreur: Une erreur de base de données s'est produite.";
        }
    }

    /**
     * Logs in a user
     *
     * @param username the username
     * @param password the password
     * @return a message indicating success or failure
     */
    public String login(String username, String password) {
        try {
            // Validate input
            if (username == null || username.trim().isEmpty()) {
                return "Erreur: Le nom d'utilisateur ne peut pas être vide.";
            }
            if (password == null || password.trim().isEmpty()) {
                return "Erreur: Le mot de passe ne peut pas être vide.";
            }

            // Check if user exists and password is correct
            Map<String, Object> user = userDatabase.getUserByUser(username);
            if (user == null) {
                return "Erreur: Utilisateur introuvable.";
            }

            // Verify password using hash
            String storedHash = (String) user.get("password");
            String salt = (String) user.get("salt");
            if (!verifyPassword(password, storedHash, salt)) {
                return "Erreur: Mot de passe incorrect.";
            }

            // Set current user
            this.currentUser = username;
            return "Connexion réussie. Bienvenue " + username + ".";
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion: " + e.getMessage());
            e.printStackTrace();
            return "Erreur: Une erreur de base de données s'est produite.";
        }
    }

    /**
     * Logs out the current user
     *
     * @return a message indicating success
     */
    public String logout() {
        if (currentUser == null) {
            return "Erreur: Aucun utilisateur n'est actuellement connecté.";
        }
        String username = currentUser;
        this.currentUser = null;
        return "Déconnexion réussie. Au revoir " + username + ".";
    }

    /**
     * Gets the currently logged-in user
     *
     * @return the username of the current user, or null if no user is logged in
     */
    public String getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks if a user is currently logged in
     *
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Changes the password of the current user
     *
     * @param oldPassword the old password
     * @param newPassword the new password
     * @return a message indicating success or failure
     */
    public String changePassword(String oldPassword, String newPassword) {
        try {
            if (!isLoggedIn()) {
                return "Erreur: Aucun utilisateur n'est actuellement connecté.";
            }

            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                return "Erreur: L'ancien mot de passe ne peut pas être vide.";
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return "Erreur: Le nouveau mot de passe ne peut pas être vide.";
            }

            // Get current user info
            Map<String, Object> user = userDatabase.getUserByUser(currentUser);
            if (user == null) {
                return "Erreur: Utilisateur introuvable.";
            }

            // Verify old password
            String storedHash = (String) user.get("password");
            String salt = (String) user.get("salt");
            if (!verifyPassword(oldPassword, storedHash, salt)) {
                return "Erreur: L'ancien mot de passe est incorrect.";
            }

            // Update password
            String newSalt = CryptoService.generateSalt();
            String newHashedPassword = CryptoService.hashPassword(newPassword, newSalt);
            int userId = (int) user.get("id");
            boolean success = userDatabase.updateUser(userId, currentUser, newHashedPassword, newSalt);
            if (success) {
                return "Mot de passe changé avec succès.";
            } else {
                return "Erreur: Impossible de changer le mot de passe.";
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du changement de mot de passe: " + e.getMessage());
            e.printStackTrace();
            return "Erreur: Une erreur de base de données s'est produite.";
        }
    }
}
