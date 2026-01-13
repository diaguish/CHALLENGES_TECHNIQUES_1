package application;
import domain.repository.FileRepository;
import infrastructures.filesystem.LocalFileRepository;
import domain.exception.*;
import java.nio.file.Path;

public class FileService {
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
        try {
            return getInstance().repository.listFiles(directoryPath);
        } catch (IllegalArgumentException e) {
            return "Invalid directory: " + e.getMessage();
        } catch (UnknowException e) {
            return "Unknown error: " + e.getMessage();
        }
    }
}