package domain.repository;
import domain.exception.FileAlreadyExistsException;
import domain.exception.FileNotFoundException;  
import domain.exception.FileNotReadableException;   
import domain.exception.UnknowException;   
import java.nio.file.Path;

/**
 * Interface for file repository operations.
 * Defines methods for CRUD operations on files and directories.
 */
public interface FileRepository {
    /**
     * Creates a new file in the specified directory.
     * 
     * @param directory the directory path
     * @param filename the name of the file to create
     * @throws FileAlreadyExistsException if the file already exists
     * @throws IllegalArgumentException if the filename is invalid
     * @throws UnknowException for any other errors
     */
    void create(Path directory, String filename) throws FileAlreadyExistsException, IllegalArgumentException, UnknowException;
    
    /**
     * Creates a new directory.
     * 
     * @param directory the parent directory path
     * @param directoryName the name of the directory to create
     * @throws IllegalArgumentException if the directory name is invalid
     * @throws UnknowException for any other errors
     * @throws FileAlreadyExistsException if the directory already exists
     */
    void createRepository(Path directory, String directoryName) throws IllegalArgumentException, UnknowException, FileAlreadyExistsException;
    
    /**
     * Deletes a file from the specified directory.
     * 
     * @param directory the directory path
     * @param filename the name of the file to delete
     * @throws FileNotFoundException if the file does not exist
     * @throws IllegalArgumentException if the filename is invalid
     * @throws UnknowException for any other errors
     */
    void delete(Path directory, String filename) throws FileNotFoundException, IllegalArgumentException, UnknowException;
    
    /**
     * Reads the content of a file.
     * 
     * @param directory the directory path
     * @param filename the name of the file to read
     * @return the content of the file
     * @throws FileNotFoundException if the file does not exist
     * @throws FileNotReadableException if the file cannot be read
     * @throws IllegalArgumentException if the filename is invalid
     * @throws UnknowException for any other errors
     */
    String read(Path directory, String filename) throws FileNotFoundException, FileNotReadableException, IllegalArgumentException, UnknowException;
    
    /**
     * Lists all files in the specified directory.
     * 
     * @param directoryName the directory path
     * @return a formatted string of file names
     * @throws IllegalArgumentException if the directory is invalid
     * @throws UnknowException for any other errors
     */
    String listFiles(Path directoryName) throws IllegalArgumentException, UnknowException;
    
    /**
     * Checks if a path is a directory.
     * 
     * @param path the path to check
     * @return true if the path is a directory, false otherwise
     * @throws IllegalArgumentException if the path is invalid
     */
    Boolean isDirectory(Path path) throws IllegalArgumentException;
    
    /**
     * Updates the content of a file.
     * 
     * @param directory the directory path
     * @param filename the name of the file to update
     * @param newContent the new content for the file
     * @return a success message
     * @throws FileNotFoundException if the file does not exist
     * @throws FileNotReadableException if the file cannot be read
     * @throws IllegalArgumentException if the filename is invalid
     * @throws UnknowException for any other errors
     */
    String update(Path directory, String filename, String newContent) throws FileNotFoundException, FileNotReadableException, IllegalArgumentException, UnknowException;
}
