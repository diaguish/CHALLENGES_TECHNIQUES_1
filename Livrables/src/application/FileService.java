package application;
import domain.repository.FileRepository;
import infrastructures.filesystem.LocalFileRepository;
import domain.exception.*;
import java.nio.file.Path;
import infrastructures.database.Journalisation;
import java.sql.SQLException;

public class FileService {
    /**
     * Singleton service class to handle file operations.
     */
    private static FileService instance;
    private final FileRepository repository;
    private Journalisation journalisation;
    
    private FileService() throws SQLException {
        this.repository = new LocalFileRepository();
        this.journalisation = Journalisation.getInstance();
    }
    
    public static synchronized FileService getInstance() throws SQLException {
        if (instance == null) {
            instance = new FileService();
        }
        return instance;
    }
    
    public String createFile(Path directory, String filename) {
        /**
         * Create a new file in the specified directory.
         * directory: Path - the path of the directory where the file will be created
         * filename: String - the name of the file to be created
         * return success or error message
         */
        try {
            repository.create(directory, filename);
            journalisation.createLog("system", "CREATE", directory.resolve(filename).toString());
            return "File created successfully";
        } catch (FileAlreadyExistsException e) {
            journalisation.createLog("system", "CREATE_FAILED", directory.resolve(filename).toString());
            return "Cannot create file: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            journalisation.createLog("system", "CREATE_FAILED", directory.resolve(filename).toString());
            return "Invalid filename: " + e.getMessage();
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        } catch (UnknowException e) {
            journalisation.createLog("system", "CREATE_FAILED", directory.resolve(filename).toString());
            return "Unknown error: " + e.getMessage();
        }
    }

    public String deleteFile(Path directory, String filename) {
        /**
        * Delete a file in the specified directory.
        * directory: Path - the path of the directory where the file is located
        * filename: String - the name of the file to be deleted
        * return success or error message
        */
        try {
            repository.delete(directory, filename);
            journalisation.createLog("system", "DELETE", directory.resolve(filename).toString());
            return "File deleted successfully";
        } catch (FileNotFoundException e) {
            journalisation.createLog("system", "DELETE_FAILED", directory.resolve(filename).toString());
            return "Cannot delete file: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            journalisation.createLog("system", "DELETE_FAILED", directory.resolve(filename).toString());
            return "Invalid filename: " + e.getMessage();
        } catch (SQLException e) {
            journalisation.createLog("system", "DELETE_FAILED", directory.resolve(filename).toString());
            return "Database error: " + e.getMessage();
        } catch (UnknowException e) {
            journalisation.createLog("system", "DELETE_FAILED", directory.resolve(filename).toString());
            return "Unknown error: " + e.getMessage();
        }
    }

    public String readFile(Path directory, String filename) {
        /**
        * Read the content of a file in the specified directory.
        * directory: Path - the path of the directory where the file is located
        * filename: String - the name of the file to be read
        * return the content of the file or an error message
        */
        try {
            String ret = repository.read(directory, filename);
            journalisation.createLog("system", "READ", directory.resolve(filename).toString());
            return ret;
        } catch (FileNotFoundException e) {
            journalisation.createLog("system", "READ_FAILED", directory.resolve(filename).toString());
            return "Cannot read file: " + e.getMessage();
        } catch (FileNotReadableException e) {
            journalisation.createLog("system", "READ_FAILED", directory.resolve(filename).toString());
            return "File not readable: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            journalisation.createLog("system", "READ_FAILED", directory.resolve(filename).toString());
            return "Invalid filename: " + e.getMessage();
        } catch (SQLException e) {
            journalisation.createLog("system", "READ_FAILED", directory.resolve(filename).toString());
            return "Database error: " + e.getMessage();
        } catch (UnknowException e) {
            journalisation.createLog("system", "READ_FAILED", directory.resolve(filename).toString());
            return "Unknown error: " + e.getMessage();
        }
    }

    public String createRepository(Path directory, String directoryName) {
        /**
        * Create a new directory in the specified directory.
        * directory: Path - the path of the directory where the new directory will be created
        * directoryName: String - the name of the new directory to be created
        * return success or error message
        */
        try {
            repository.createRepository(directory, directoryName);
            journalisation.createLog("system", "CREATE_REPO", directory.resolve(directoryName).toString());
            return "Repository created successfully";
        } catch (FileAlreadyExistsException e) {
            journalisation.createLog("system", "CREATE_REPO_FAILED", directory.resolve(directoryName).toString());
            return "Cannot create repository: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            journalisation.createLog("system", "CREATE_REPO_FAILED", directory.resolve(directoryName).toString());
            return "Invalid directory name: " + e.getMessage();
        } catch (SQLException e) {
            journalisation.createLog("system", "CREATE_REPO_FAILED", directory.resolve(directoryName).toString());
            return "Database error: " + e.getMessage();
        } catch (UnknowException e) {
            journalisation.createLog("system", "CREATE_REPO_FAILED", directory.resolve(directoryName).toString());
            return "Unknown error: " + e.getMessage();
        }
    }

    public String listFiles(Path directoryPath) {
        /**
        * List all files in the specified directory.
        * directoryPath: Path - the path of the directory to list files from
        * return a formatted string of file names or an error message
        */
        try {
            String ret = getInstance().repository.listFiles(directoryPath);
            journalisation.createLog("system", "LIST_FILES", directoryPath.toString());
            return ret;
        }catch (SQLException e) {
            journalisation.createLog("system", "LIST_FILES_FAILED", directoryPath.toString());
            return "Database error: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            journalisation.createLog("system", "LIST_FILES_FAILED", directoryPath.toString());
            return "Invalid directory: " + e.getMessage();
        } catch (UnknowException e) {
            journalisation.createLog("system", "LIST_FILES_FAILED", directoryPath.toString());
            return "Unknown error: " + e.getMessage();
        }
    }

    public String updateFile(Path directory, String filename, String newContent) {
        /**
        * Update the content of a file in the specified directory.
        * directory: Path - the path of the directory where the file is located
        * filename: String - the name of the file to be updated
        * newContent: String - the new content to write to the file
        * return success or error message
        */
        try {
            String ret = repository.update(directory, filename, newContent);
            journalisation.createLog("system", "UPDATE", directory.resolve(filename).toString());
            return ret;
        } catch (FileNotFoundException e) {
            journalisation.createLog("system", "UPDATE_FAILED", directory.resolve(filename).toString());
            return "Cannot update file: " + e.getMessage();
        } catch (FileNotReadableException e) {
            journalisation.createLog("system", "UPDATE_FAILED", directory.resolve(filename).toString());
            return "File not readable: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            journalisation.createLog("system", "UPDATE_FAILED", directory.resolve(filename).toString());
            return "Invalid filename: " + e.getMessage();
        } catch (SQLException e) {
            journalisation.createLog("system", "UPDATE_FAILED", directory.resolve(filename).toString());
            return "Database error: " + e.getMessage();
        } catch (UnknowException e) {
            journalisation.createLog("system", "UPDATE_FAILED", directory.resolve(filename).toString());
            return "Unknown error: " + e.getMessage();
        }
    }
}