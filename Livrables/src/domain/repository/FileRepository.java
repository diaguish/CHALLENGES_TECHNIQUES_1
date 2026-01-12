package domain.repository;
import domain.exception.FileAlreadyExistsException;
import domain.exception.FileNotFoundException;  
import domain.exception.FileNotReadableException;   
import domain.exception.UnknowException;   


public interface FileRepository {
    void create(String filename) throws FileAlreadyExistsException, IllegalArgumentException, UnknowException;
    void createRepository(String directoryName) throws IllegalArgumentException, UnknowException, FileAlreadyExistsException;
    void delete(String filename) throws FileNotFoundException, IllegalArgumentException, UnknowException;
    //void deleteRepository(String directoryName) throws FileNotFoundException, IllegalArgumentException, UnknowException;
    String read(String filename) throws FileNotFoundException, FileNotReadableException, IllegalArgumentException, UnknowException;
}
