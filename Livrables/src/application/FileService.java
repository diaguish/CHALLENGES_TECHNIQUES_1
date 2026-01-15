package application;
import domain.repository.FileRepository;
import infrastructures.filesystem.LocalFileRepository;
import infrastructures.security.HashService;
import infrastructures.security.IntegrityStore;
import domain.exception.*;
import java.nio.file.Path;
import infrastructures.database.Journalisation;
import java.sql.SQLException;
import java.nio.file.Files;
import java.io.IOException;
import java.util.Map;
import infrastructures.security.CryptoService;
import infrastructures.database.FilePassword;
import infrastructures.database.User;
import application.WorkingContext;


/**
 * Singleton service class to handle file operations.
 * Manages file creation, deletion, reading, updating and directory operations.
 */
public class FileService {
    private static FileService instance;
    private final FileRepository repository;
    private Journalisation journalisation;
    private FilePassword filePassword;
    private HashService hashService ;
    private IntegrityStore integrityStore;
    private UserService userService;
    private User userDatabase;
    private WorkingContext workingContext;
    
    /**
     * Checks if integrity checking is enabled.
     * 
     * @return true if both hashService and integrityStore are initialized
     */
    private boolean integrityEnabled() {
        return hashService != null && integrityStore != null;
    }

    /**
     * Private constructor for singleton pattern.
     * Initializes all required services and repositories.
     * 
     * @throws SQLException if database initialization fails
     */
    private FileService() throws SQLException {
        this.repository = new LocalFileRepository();
        this.journalisation = Journalisation.getInstance();
        this.filePassword = FilePassword.getInstance();
        this.userDatabase = User.getInstance();
        this.userService = UserService.getInstance();
        this.workingContext = WorkingContext.getInstance("root_app");
    }
    
    /**
     * Gets the singleton instance of FileService.
     * 
     * @return the FileService instance
     * @throws SQLException if initialization fails
     */
    public static synchronized FileService getInstance() throws SQLException {
        if (instance == null) {
            instance = new FileService();
        }
        return instance;
    }
    
    /**
     * Creates a new file in the specified directory.
     * Encrypts the file content using the current user's password.
     * 
     * @param directory the directory where the file will be created
     * @param filename the name of the file to create
     * @return success or error message
     */
    public String createFile(Path directory, String filename) {
        /**
         * Create a new file in the specified directory.
         * directory: Path - the path of the directory where the file will be created
         * filename: String - the name of the file to be created
         * return success or error message
         */
        try {
            CryptoService cryptoService = new CryptoService();

            String currentUser = userService.getCurrentUser();
            String userHashedPassword = userDatabase.getUserByUser(currentUser).get("password").toString();


            String[] keyAndSalt = cryptoService.generateKey(userHashedPassword);
            String encryptedContent = cryptoService.encryptText("", keyAndSalt[0]);
            filePassword.createFilePassword(workingContext.displayPath(workingContext.getCurrent()) + "/" + filename, currentUser, keyAndSalt[1]);

            repository.create(directory, filename);
            journalisation.createLog(userService.getCurrentUser(), "CREATE", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            if (integrityEnabled()) {
            Path filePath = directory.resolve(filename).normalize();
            String hash = hashService.sha256(filePath);
            long size = Files.size(filePath);
            integrityStore.appendEntry(filePath, hash, size);
    }
            return "File created successfully";
        } catch (FileAlreadyExistsException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "CREATE_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            } catch (SQLException se) {
                return "Cannot create file: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "Cannot create file: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "CREATE_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            } catch (SQLException se) {
                return "Invalid filename: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "Invalid filename: " + e.getMessage();
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        } catch (IOException e) {
            return "IO error: " + e.getMessage();
        } catch (UnknowException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "CREATE_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            } catch (SQLException se) {
                return "Unknown error: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "Unknown error: " + e.getMessage();
        }
    }

    /**
     * Checks the integrity of a file against stored hash and size.
     * 
     * @param directory the directory containing the file
     * @param filename the name of the file to check
     * @return null if integrity is valid, error message otherwise
     */
    private String checkIntegrity(Path directory, String filename) {
    if (!integrityEnabled()) return null;

        try {
            Path filePath = directory.resolve(filename).normalize();
        IntegrityStore.IntegrityEntry last = integrityStore.loadLastEntry(filePath);
        if (last == null) {
            // pas encore d'entrée => on laisse passer
            return null;
        }
        if ("DELETED".equals(last.hash)) {
    // Si l'historique dit "supprimé", alors le fichier ne devrait plus exister
    if (Files.exists(filePath)) {
        journalisation.createLog(userService.getCurrentUser(), "INTEGRITY_MISMATCH", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
        return "Integrite compromise : fichier present alors qu'il est marque supprime.";
    }
    return null; // ok: il est bien absent
}


        String currentHash = hashService.sha256(filePath);
        long currentSize = Files.size(filePath);

        boolean ok = last.hash.equals(currentHash) && last.size == currentSize;
        if (!ok) {
            journalisation.createLog(userService.getCurrentUser(), "INTEGRITY_MISMATCH", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            return "⚠️ Intégrité compromise : le fichier a été modifié hors application.";
        }

        return null;
    } catch (IOException | SQLException e) {
        return "Erreur lors de la vérification d'intégrité : " + e.getMessage();
    }
}


    /**
     * Deletes a file from the specified directory.
     * 
     * @param directory the directory where the file is located
     * @param filename the name of the file to delete
     * @return success or error message
     */
    public String deleteFile(Path directory, String filename) {
        try {
            String integrityError = checkIntegrity(directory, filename);
            if (integrityError != null) {
                return integrityError;
            }
            if (integrityEnabled()) {
                Path filePath = directory.resolve(filename).normalize();
                integrityStore.appendDeleteEvent(filePath);
            }
             
            String Owner = filePassword.getFilePasswordByFilename(workingContext.displayPath(workingContext.getCurrent()) + "/" + filename).get("user").toString();
            if (Owner == null || !userService.getCurrentUser().equals(Owner)) {
                return "Cannot delete file: current user is not the owner";
            }

            repository.delete(directory, filename);
            journalisation.createLog(userService.getCurrentUser(), "DELETE", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            return "File deleted successfully";
        } catch (FileNotFoundException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "DELETE_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            } catch (SQLException se) {
                return "Cannot delete file: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "Cannot delete file: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "DELETE_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            } catch (SQLException se) {
                return "Invalid filename: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "Invalid filename: " + e.getMessage();
        } catch (SQLException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "DELETE_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            } catch (SQLException se) {
                // Log error silently
            }
            return "Database error: " + e.getMessage();
        } catch (UnknowException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "DELETE_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            } catch (SQLException se) {
                return "Unknown error: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "Unknown error: " + e.getMessage();
        }
    }

    /**
     * Reads the content of a file from the specified directory.
     * Decrypts the file content using the current user's password.
     * 
     * @param directory the directory where the file is located
     * @param filename the name of the file to read
     * @return the decrypted content or error message
     */
    public String readFile(Path directory, String filename) {
        try {
            String integrityError = checkIntegrity(directory, filename);
            if (integrityError != null) {
                return integrityError;
            }   

            String owner = filePassword.getFilePasswordByFilename(workingContext.displayPath(workingContext.getCurrent()) + "/" + filename).get("user").toString();
            if (owner == null || !userService.getCurrentUser().equals(owner)) {
                return "Cannot read file: current user is not the owner";
            }
            String ret = repository.read(directory, filename);
            //decrypt content
            CryptoService cryptoService = new CryptoService();
            String currentUser = userService.getCurrentUser();
            String userHashedPassword = userDatabase.getUserByUser(currentUser).get("password").toString();
            String salt = filePassword.getFilePasswordByFilename(workingContext.displayPath(workingContext.getCurrent()) + "/" + filename).get("salt").toString();
            String[] keyAndSalt = cryptoService.generateKey(userHashedPassword, salt);

            String decryptedContent = cryptoService.decryptText(ret, keyAndSalt[0]);
            journalisation.createLog(userService.getCurrentUser(), "READ", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            return decryptedContent;
        } catch (FileNotFoundException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "READ_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            } catch (SQLException se) {
                return "Cannot read file: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "Cannot read file: " + e.getMessage();
        } catch (FileNotReadableException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "READ_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            } catch (SQLException se) {
                return "File not readable: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "File not readable: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "READ_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            } catch (SQLException se) {
                return "Invalid filename: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "Invalid filename: " + e.getMessage();
        } catch (SQLException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "READ_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            } catch (SQLException se) {
                // Log error silently
            }
            return "Database error: " + e.getMessage();
        } catch (HashException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "READ_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            } catch (SQLException se) {
                return "Hash error: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "Hash error: " + e.getMessage();
        } catch (UnknowException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "READ_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            } catch (SQLException se) {
                return "Unknown error: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "Unknown error: " + e.getMessage();
        }
    }

    /**
     * Creates a new repository (directory) in the specified directory.
     * 
     * @param directory the parent directory path
     * @param directoryName the name of the new directory to create
     * @return success or error message
     */
    public String createRepository(Path directory, String directoryName) {
        try {
            repository.createRepository(directory, directoryName);
            journalisation.createLog(userService.getCurrentUser(), "CREATE_REPO", workingContext.displayPath(workingContext.getCurrent()) + "/" + directoryName);
            return "Repository created successfully";
        } catch (FileAlreadyExistsException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "CREATE_REPO_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + directoryName);
            } catch (SQLException se) {
                return "Cannot create repository: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "Cannot create repository: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "CREATE_REPO_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + directoryName);
            } catch (SQLException se) {
                return "Invalid directory name: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "Invalid directory name: " + e.getMessage();
        } catch (SQLException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "CREATE_REPO_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + directoryName);
            } catch (SQLException se) {
                // Log error silently
            }
            return "Database error: " + e.getMessage();
        } catch (UnknowException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "CREATE_REPO_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + directoryName);
            } catch (SQLException se) {
                return "Unknown error: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "Unknown error: " + e.getMessage();
        }
    }

    /**
     * Lists all files and directories in the specified directory.
     * 
     * @param directoryPath the path of the directory to list files from
     * @return a formatted string of file names or error message
     */
    public String listFiles(Path directoryPath) {
        try {
            String ret = getInstance().repository.listFiles(directoryPath);
            journalisation.createLog(userService.getCurrentUser(), "LIST_FILES", workingContext.displayPath(workingContext.getCurrent()));
            return ret;

        }catch (SQLException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "LIST_FILES_FAILED", workingContext.displayPath(workingContext.getCurrent()));
            } catch (SQLException se) {
                return "Database error: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "Database error: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "LIST_FILES_FAILED", workingContext.displayPath(workingContext.getCurrent()));
            } catch (SQLException se) {
                return "Invalid directory: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "Invalid directory: " + e.getMessage();
        } catch (UnknowException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "LIST_FILES_FAILED", workingContext.displayPath(workingContext.getCurrent()));
            } catch (SQLException se) {
                return "Unknown error: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "Unknown error: " + e.getMessage();
        }
    }

    /**
     * Updates the content of a file in the specified directory.
     * Encrypts the new content using the current user's password.
     * 
     * @param directory the directory where the file is located
     * @param filename the name of the file to update
     * @param newContent the new content to write to the file
     * @return success or error message
     */
    public String updateFile(Path directory, String filename, String newContent) {
        try {
            Map<String, Object> filePass = filePassword.getFilePasswordByFilename(workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            String owner = filePass.get("user").toString();
            String salt = filePass.get("salt").toString();
            if (owner == null || !userService.getCurrentUser().equals(owner)) {
                return "Cannot update file: current user is not the owner";
            }
            CryptoService cryptoService = new CryptoService();
            String currentUser = userService.getCurrentUser();
            String userHashedPassword = userDatabase.getUserByUser(currentUser).get("password").toString();
            String[] keyAndSalt = cryptoService.generateKey(userHashedPassword, salt);
            String encryptedContent = cryptoService.encryptText(newContent, keyAndSalt[0]);

            String ret = repository.update(directory, filename, encryptedContent);
            journalisation.createLog(userService.getCurrentUser(), "UPDATE", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            if (integrityEnabled()) {
            Path filePath = directory.resolve(filename).normalize();
                String hash = hashService.sha256(filePath);
                long size = java.nio.file.Files.size(filePath);
                integrityStore.appendEntry(filePath, hash, size);
            }
            return ret;
        } catch (FileNotFoundException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "UPDATE_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            } catch (SQLException se) {
                return "Cannot update file: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "Cannot update file: " + e.getMessage();
        } catch (FileNotReadableException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "UPDATE_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            } catch (SQLException se) {
                return "File not readable: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "File not readable: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "UPDATE_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            } catch (SQLException se) {
                return "Invalid filename: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "Invalid filename: " + e.getMessage();
        } catch (SQLException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "UPDATE_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            } catch (SQLException se) {
                // Log error silently
            }
            return "Database error: " + e.getMessage();
        } catch (UnknowException e) {
            try {
                journalisation.createLog(userService.getCurrentUser(), "UPDATE_FAILED", workingContext.displayPath(workingContext.getCurrent()) + "/" + filename);
            } catch (SQLException se) {
                return "Unknown error: " + e.getMessage() + " - Database error: " + se.getMessage();
            }
            return "Unknown error: " + e.getMessage();
        }catch (java.io.IOException e) {
    return "Cannot compute integrity: " + e.getMessage();
        } 

    }

    /**
     * Configures integrity checking for files.
     * Initializes the HashService and IntegrityStore.
     * 
     * @param rootDir the root directory for integrity storage
     */
    public void configureIntegrity(Path rootDir) {
        this.hashService = new HashService();
        this.integrityStore = new IntegrityStore(rootDir);
    }

}