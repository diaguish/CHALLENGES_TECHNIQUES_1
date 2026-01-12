package application;
import domain.repository.FileRepository;
import infrastructures.filesystem.LocalFileRepository;
import domain.exception.*;

public class FileService {
    private final FileRepository repository;
    public FileService() {
        this.repository = new LocalFileRepository();
    }
    
    public String createFile(String filename) {
        try {
            repository.create(filename);
            return "File created successfully";
        } catch (FileAlreadyExistsException e) {
            return "Cannot create file: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "Invalid filename: " + e.getMessage();
        } catch (UnknowException e) {
            return "Unknown error: " + e.getMessage();
        }
    }

    public String deleteFile(String filename) {
        try {
            repository.delete(filename);
            return "File deleted successfully";
        } catch (FileNotFoundException e) {
            return "Cannot delete file: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "Invalid filename: " + e.getMessage();
        } catch (UnknowException e) {
            return "Unknown error: " + e.getMessage();
        }
    }

    public String readFile(String filename) {
        try {
            return repository.read(filename);
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

    public String createRepository(String directoryName) {
        try {
            repository.createRepository(directoryName);
            return "Repository created successfully";
        } catch (FileAlreadyExistsException e) {
            return "Cannot create repository: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "Invalid directory name: " + e.getMessage();
        } catch (UnknowException e) {
            return "Unknown error: " + e.getMessage();
        }
    }
}