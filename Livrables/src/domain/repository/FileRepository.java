package domain.repository;
import domain.exception.FileAlreadyExistsException;
import domain.exception.FileNotFoundException;  
import domain.exception.FileNotReadableException;   
import domain.exception.UnknowException;   
import java.nio.file.Path;


public interface FileRepository {
    void create(Path directory, String filename) throws FileAlreadyExistsException, IllegalArgumentException, UnknowException;
    void createRepository(Path directory, String directoryName) throws IllegalArgumentException, UnknowException, FileAlreadyExistsException;
    void delete(Path directory, String filename) throws FileNotFoundException, IllegalArgumentException, UnknowException;
    //void deleteRepository(String directoryName) throws FileNotFoundException, IllegalArgumentException, UnknowException;
    String read(Path directory, String filename) throws FileNotFoundException, FileNotReadableException, IllegalArgumentException, UnknowException;
    String listFiles(Path directoryName) throws IllegalArgumentException, UnknowException;
    Boolean isDirectory(Path path);
}
