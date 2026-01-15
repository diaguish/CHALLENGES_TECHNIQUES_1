package application;

import domain.exception.FileAccessException;

import java.nio.file.Path;
import java.nio.file.Paths;
import infrastructures.filesystem.LocalFileRepository;
import infrastructures.database.Journalisation;
import java.sql.SQLException;
import application.UserService;

/**
 * Manages the working context (current directory) for file operations.
 * Ensures that all operations stay within a designated root directory.
 */
public class WorkingContext {

    private final Path root;
    private Path current;
    private LocalFileRepository fileRepository = new LocalFileRepository();
    private Journalisation journalisation;
    private static WorkingContext instance;
    private static UserService userService;

    /**
     * Private constructor for singleton pattern.
     * Initializes the working context with a root directory and sets the current directory to root.
     * 
     * @param rootDirectory the path of the root directory
     * @throws SQLException if database initialization fails
     */
    private WorkingContext(String rootDirectory) throws SQLException {
        this.root = Paths.get(rootDirectory).toAbsolutePath().normalize();
        this.current = root;
        this.journalisation = Journalisation.getInstance();
        userService = UserService.getInstance();
        
    }

    /**
     * Gets the singleton instance of WorkingContext.
     * 
     * @param rootDirectory the root directory path
     * @return the WorkingContext instance
     * @throws SQLException if initialization fails
     */
    public static synchronized WorkingContext getInstance(String rootDirectory) throws SQLException {
        if (instance == null) {
            instance = new WorkingContext(rootDirectory);
        }
        return instance;
    }

    /**
     * Formats a path relative to the root directory.
     * 
     * @param path the path to format
     * @return the formatted relative path
     */
    private String formatPath(Path path) {
        Path relative = root.relativize(path);
        String rel = relative.toString().replace('\\', '/');
        return rel.isEmpty() ? "/" : "/" + rel;
    }

    /**
     * Gets the display representation of a path.
     * 
     * @param path the path to display
     * @return the formatted path for display
     */
    public String displayPath(Path path) {
        return formatPath(path);
    }

    /**
     * Gets the current working directory relative to the root.
     * Logs the PWD operation to the journalisation system.
     * 
     * @return the relative path as a string
     */
    public String pwd() {
        try {
            journalisation.createLog(userService.getCurrentUser(), "PWD", getCurrent().toString());
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        } 
        return formatPath(current);
    }

    /**
     * Resolves the input path against the current directory, ensuring it stays within the root.
     * 
     * @param input the input path to resolve
     * @return the resolved Path
     * @throws FileAccessException if the resolved path is outside the root
     */
    public Path resolve(String input) {
        Path resolved = current.resolve(input).normalize();
        if (!resolved.startsWith(root)) {
            throw new FileAccessException("Sortie du répertoire autorisé interdite.");
        }
        return resolved;
    }

    /**
     * Gets the current working directory as a Path object.
     * 
     * @return the current Path
     */
    public Path getCurrent() {
        return current;
    }

    /**
     * Changes the current working directory based on the input.
     * Supports ".", "..", and "/" navigation.
     * Logs the CD operation to the journalisation system.
     * 
     * @param input the input path to change to
     * @return success or error message
     */
    public String changeDirectory(String input) {
        try {
            journalisation.createLog(userService.getCurrentUser(), "CD", input);
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
        if (input == null || input.trim().isEmpty()) {
            return "Le nom du répertoire ne peut pas être vide.";
        }
        if (input.equals("/")) {
            this.current = this.root;
            return "Changement de répertoire réussi.";
        }
        if (input.equals("..")) {
            if (this.current.equals(this.root)) {
                return "Vous êtes déjà au répertoire racine.";
            }
            this.current = this.current.getParent();
            return "Changement de répertoire réussi.";
        }
        final Path newPath = resolve(input);
        try {
            if (!fileRepository.isDirectory(newPath)) {
                return "Le répertoire spécifié n'existe pas.";
            }
            this.current = newPath;
            return "Changement de répertoire réussi.";
        } catch (IllegalArgumentException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "CD_FAILED", input);
            } catch (SQLException se) {
                return "Chemin invalide: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "Chemin invalide: " + e.getMessage();
        }
    }
    
    /**
     * Gets the root directory path.
     * 
     * @return the root Path
     */
    public Path getRoot() {
        return root;
    }
}
