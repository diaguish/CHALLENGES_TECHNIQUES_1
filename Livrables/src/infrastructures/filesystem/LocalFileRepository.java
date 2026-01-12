package infrastructures.filesystem;
import domain.exception.FileAlreadyExistsException;
import domain.exception.FileNotFoundException;
import domain.exception.FileNotReadableException;
import domain.repository.UnknowException;

public class LocalFileRepository implements FileRepository throws FileAlreadyExistsException, FileNotFoundException, IllegalArgumentException, UnknowException {
    @Override
    public void create(String filename) {
        if(java.nio.file.Files.exists(java.nio.file.Paths.get(filename))) {
            throw new FileAlreadyExistsException("File already exists: " + filename);
        }
        if(filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        try {
            java.nio.file.Files.createFile(java.nio.file.Paths.get(filename));
        } catch (java.io.IOException e) {
            throw new UnknowException("Unknown error while creating file: " + filename);
        }

    }

    @Override
    public void delete(String filename) throws FileNotFoundException, IllegalArgumentException, UnknowException {
        if(!java.nio.file.Files.exists(java.nio.file.Paths.get(filename))) {
            throw new FileNotFoundException("File not found: " + filename);
        }
        if(filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }
        
        try {
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(filename));
        } catch (java.io.IOException e) {
            throw new UnknowException("Unknown error while deleting file: " + filename);
        }
    }

    @Override
    public String read(String filename) throws FileNotFoundException, FileNotReadableException, IllegalArgumentException, UnknowException {
        if(!java.nio.file.Files.exists(java.nio.file.Paths.get(filename))) {
            throw new FileNotFoundException("File not found: " + filename);
        }

        if(!java.nio.file.Files.isReadable(java.nio.file.Paths.get(filename))) {
            throw new FileNotReadableException("File not readable: " + filename);
        }
        if(filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        try {
            return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filename)));
        } catch (java.io.IOException e) {
            throw new UnknowException("Unknown error while reading file: " + filename);
        }
    }

    @Override
    public void createRepository(String directoryName) throws IllegalArgumentException, UnknowException, FileAlreadyExistsException {
        if(directoryName == null || directoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Directory name cannot be null or empty");
        }
        if(java.nio.file.Files.exists(java.nio.file.Paths.get(directoryName))) {
            throw new FileAlreadyExistsException("Directory already exists: " + directoryName);
        }
        try {
            java.nio.file.Files.createDirectory(java.nio.file.Paths.get(directoryName));
        } catch (java.io.IOException e) {
            throw new UnknowException("Unknown error while creating directory: " + directoryName);
        }
    }
}
