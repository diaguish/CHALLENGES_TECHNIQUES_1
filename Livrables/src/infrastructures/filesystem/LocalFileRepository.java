package infrastructures.filesystem;
import domain.exception.FileAlreadyExistsException;
import domain.exception.FileNotFoundException;
import domain.exception.FileNotReadableException;
import domain.exception.UnknowException;
import domain.repository.FileRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LocalFileRepository implements FileRepository {
    @Override
    public void create(Path directory, String filename) throws FileAlreadyExistsException, IllegalArgumentException, UnknowException {
        /**
         * Create a new file in the specified directory.
         * directory: Path - the path of the directory where the file will be created
         * filename: String - the name of the file to be created
         * throws FileAlreadyExistsException if the file already exists
         * throws IllegalArgumentException if the filename is invalid
         * throws UnknowException for any other errors
         */
        if(filename == null || filename.trim().isEmpty() || directory == null) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }
        if(java.nio.file.Files.exists(java.nio.file.Paths.get(directory.toString(), filename))) {
            throw new FileAlreadyExistsException("File already exists: " + filename);
        }
        try {
            java.nio.file.Files.createFile(java.nio.file.Paths.get(directory.toString(), filename));
        } catch (java.io.IOException e) {
            throw new UnknowException("Unknown error while creating file: " + filename);
        }

    }

    @Override
    public void delete(Path directory, String filename) throws FileNotFoundException, IllegalArgumentException, UnknowException {
        /**
         * Delete a file in the specified directory.
         * directory: Path - the path of the directory where the file is located
         * filename: String - the name of the file to be deleted
         * throws FileNotFoundException if the file does not exist
         * throws IllegalArgumentException if the filename is invalid
         * throws UnknowException for any other errors
         */
        if(!java.nio.file.Files.exists(java.nio.file.Paths.get(directory.toString(), filename))) {
            throw new FileNotFoundException("File not found: " + filename);
        }
        if(filename == null || filename.trim().isEmpty() || directory == null) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }
        
        try {
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(directory.toString(), filename));
        } catch (java.io.IOException e) {
            throw new UnknowException("Unknown error while deleting file: " + filename);
        }
    }

    @Override
    public String read(Path directory, String filename) throws FileNotFoundException, FileNotReadableException, IllegalArgumentException, UnknowException {
        /**
         * Read the content of a file in the specified directory.
         * directory: Path - the path of the directory where the file is located
         * filename: String - the name of the file to be read
         * return the content of the file as a String
         * throws FileNotFoundException if the file does not exist
         * throws FileNotReadableException if the file is not readable
         * throws IllegalArgumentException if the filename is invalid
         * throws UnknowException for any other errors
         */
        if(!java.nio.file.Files.exists(java.nio.file.Paths.get(directory.toString(), filename))) {
            throw new FileNotFoundException("File not found: " + filename);
        }

        if(!java.nio.file.Files.isReadable(java.nio.file.Paths.get(directory.toString(), filename))) {
            throw new FileNotReadableException("File not readable: " + filename);
        }
        if(filename == null || filename.trim().isEmpty() || directory == null) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        try {
            return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(directory.toString(), filename)));
        } catch (java.io.IOException e) {
            throw new UnknowException("Unknown error while reading file: " + filename);
        }
    }

    @Override
    public void createRepository(Path directory, String directoryName) throws IllegalArgumentException, UnknowException, FileAlreadyExistsException {
        /**
         * Create a new directory in the specified directory.
         * directory: Path - the path of the directory where the new directory will be created
         * directoryName: String - the name of the new directory to be created
         * throws FileAlreadyExistsException if the directory already exists
         * throws IllegalArgumentException if the directory name is invalid
         * throws UnknowException for any other errors
         */
        if(directoryName == null || directoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Directory name cannot be null or empty");
        }
        if(java.nio.file.Files.exists(java.nio.file.Paths.get(directory.toString(), directoryName))) {
            throw new FileAlreadyExistsException("Directory already exists: " + directoryName);
        }
        try {
            java.nio.file.Files.createDirectory(java.nio.file.Paths.get(directory.toString(), directoryName));
        } catch (java.io.IOException e) {
            throw new UnknowException("Unknown error while creating directory: " + directoryName);
        }
    }

    @Override
    public String listFiles(Path directoryName) throws IllegalArgumentException, UnknowException {
        /**
         * List all files in the specified directory.
         * directoryName: Path - the path of the directory to list files from
         * return a formatted string of file names
         * throws IllegalArgumentException if the directory does not exist
         * throws UnknowException for any other errors
         */
        if(directoryName == null) {
            throw new IllegalArgumentException("Directory name cannot be null");
        }
        if(!Files.exists(directoryName) || !Files.isDirectory(directoryName)) {
            throw new IllegalArgumentException("Directory does not exist: " + directoryName.toString());
        }

        try {
            List<String> fileNames = new ArrayList<>();
            Files.list(directoryName).forEach(path -> fileNames.add(path.getFileName().toString()));
            return String.join("\n", fileNames);
        } catch (java.io.IOException e) {
            throw new UnknowException("Unknown error while listing files in directory: " + directoryName.toString());
        }
    }

    @Override
    public Boolean isDirectory(Path path) throws IllegalArgumentException {
        /**
         * Check if the given path is a directory.
         * path: Path - the path to check
         * return true if the path is a directory, false otherwise
         * throws IllegalArgumentException if the path is null
         */
        if(path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        return Files.isDirectory(path);
    }
}