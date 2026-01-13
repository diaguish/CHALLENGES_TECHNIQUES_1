package application;
import domain.repository.FileRepository;
import infrastructures.filesystem.LocalFileRepository;
import domain.exception.*;
import java.nio.file.Path;

public class FileService {
    /**
     * Singleton service class to handle file operations.
     */
    private static FileService instance;
    private final FileRepository repository;
    
    private FileService() {
        this.repository = new LocalFileRepository();
    }
    
    public static synchronized FileService getInstance() {
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
            return "File created successfully";
        } catch (FileAlreadyExistsException e) {
            return "Cannot create file: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "Invalid filename: " + e.getMessage();
        } catch (UnknowException e) {
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
            return "File deleted successfully";
        } catch (FileNotFoundException e) {
            return "Cannot delete file: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "Invalid filename: " + e.getMessage();
        } catch (UnknowException e) {
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
            return repository.read(directory, filename);
        } catch (FileNotFoundException e) {
            return "Cannot read file: " + e.getMessage();
        } catch (FileNotReadableException e) {
            return "File not readable: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "Invalid filename: " + e.getMessage();
        } catch (UnknowException e) {
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
            return "Repository created successfully";
        } catch (FileAlreadyExistsException e) {
            return "Cannot create repository: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "Invalid directory name: " + e.getMessage();
        } catch (UnknowException e) {
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
            return getInstance().repository.listFiles(directoryPath);
        } catch (IllegalArgumentException e) {
            return "Invalid directory: " + e.getMessage();
        } catch (UnknowException e) {
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
            return repository.update(directory, filename, newContent);
        } catch (FileNotFoundException e) {
            return "Cannot update file: " + e.getMessage();
        } catch (FileNotReadableException e) {
            return "File not readable: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "Invalid filename: " + e.getMessage();
        } catch (UnknowException e) {
            return "Unknown error: " + e.getMessage();
        }
    }
}