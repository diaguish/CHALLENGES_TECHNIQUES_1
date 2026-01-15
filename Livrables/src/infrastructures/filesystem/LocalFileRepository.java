package infrastructures.filesystem;
import domain.exception.FileAlreadyExistsException;
import domain.exception.FileNotFoundException;
import domain.exception.FileNotReadableException;
import domain.exception.UnknowException;
import domain.repository.FileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Local file system implementation of FileRepository.
 * Provides file and directory operations on the local file system.
 */
public class LocalFileRepository implements FileRepository {
    private static final String RESET = "\u001B[0m";
    private static final String BLUE = "\u001B[34m";
    private static final String WHITE = "\u001B[37m";

    /**
     * Creates a new file in the specified directory.
     * 
     * @param directory the directory path where the file will be created
     * @param filename the name of the file to create
     * @throws FileAlreadyExistsException if the file already exists
     * @throws IllegalArgumentException if the filename is invalid
     * @throws UnknowException for any other file system errors
     */
    @Override
    public void create(Path directory, String filename) throws FileAlreadyExistsException, IllegalArgumentException, UnknowException {
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

    /**
     * Deletes a file from the specified directory.
     * 
     * @param directory the directory path containing the file
     * @param filename the name of the file to delete
     * @throws FileNotFoundException if the file does not exist
     * @throws IllegalArgumentException if the filename is invalid
     * @throws UnknowException for any other file system errors
     */
    @Override
    public void delete(Path directory, String filename) throws FileNotFoundException, IllegalArgumentException, UnknowException {
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

    /**
     * Reads the content of a file.
     * 
     * @param directory the directory path containing the file
     * @param filename the name of the file to read
     * @return the content of the file as a String
     * @throws FileNotFoundException if the file does not exist
     * @throws FileNotReadableException if the file is not readable
     * @throws IllegalArgumentException if the filename is invalid
     * @throws UnknowException for any other file system errors
     */
    @Override
    public String read(Path directory, String filename) throws FileNotFoundException, FileNotReadableException, IllegalArgumentException, UnknowException {
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

    /**
     * Creates a new directory in the specified location.
     * 
     * @param directory the parent directory path
     * @param directoryName the name of the new directory to create
     * @throws IllegalArgumentException if the directory name is invalid
     * @throws UnknowException for any other file system errors
     * @throws FileAlreadyExistsException if the directory already exists
     */
    @Override
    public void createRepository(Path directory, String directoryName) throws IllegalArgumentException, UnknowException, FileAlreadyExistsException {
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

    /**
     * Lists all files and directories in the specified directory.
     * Directories are displayed in blue with a trailing slash, files in white.
     * 
     * @param directoryName the path of the directory to list
     * @return a formatted string of file and directory names
     * @throws IllegalArgumentException if the directory does not exist or is invalid
     * @throws UnknowException for any other file system errors
     */
    @Override
    public String listFiles(Path directoryName) throws IllegalArgumentException, UnknowException {
        if(directoryName == null) {
            throw new IllegalArgumentException("Directory name cannot be null");
        }
        if(!Files.exists(directoryName) || !Files.isDirectory(directoryName)) {
            throw new IllegalArgumentException("Directory does not exist: " + directoryName.toString());
        }

         try {
        List<String> items = new ArrayList<>();

        Files.list(directoryName).forEach(path -> {
            String name = path.getFileName().toString();

            if (Files.isDirectory(path)) {
                items.add(BLUE + name + "/" + RESET);
            } else {
                items.add(WHITE + name + RESET);
            }
        });

        return String.join("\n", items);
    } catch (IOException e) {
        throw new UnknowException("Unknown error while listing files in directory: " + directoryName);
    }
    }

    /**
     * Checks if the given path is a directory.
     * 
     * @param path the path to check
     * @return true if the path is a directory, false otherwise
     * @throws IllegalArgumentException if the path is null
     */
    @Override
    public Boolean isDirectory(Path path) throws IllegalArgumentException {
        if(path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        return Files.isDirectory(path);
    }

    /**
     * Updates the content of a file.
     * 
     * @param directory the directory path containing the file
     * @param filename the name of the file to update
     * @param newContent the new content to write to the file
     * @return the updated content of the file
     * @throws FileNotFoundException if the file does not exist
     * @throws FileNotReadableException if the file is not writable
     * @throws IllegalArgumentException if the filename is invalid
     * @throws UnknowException for any other file system errors
     */
    @Override
    public String update(Path directory, String filename, String newContent) throws FileNotFoundException, FileNotReadableException, IllegalArgumentException, UnknowException {
        if(!java.nio.file.Files.exists(java.nio.file.Paths.get(directory.toString(), filename))) {
            throw new FileNotFoundException("File not found: " + filename);
        }

        if(!java.nio.file.Files.isWritable(java.nio.file.Paths.get(directory.toString(), filename))) {
            throw new FileNotReadableException("File not writable: " + filename);
        }
        if(filename == null || filename.trim().isEmpty() || directory == null) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        try {
            java.nio.file.Files.write(java.nio.file.Paths.get(directory.toString(), filename), newContent.getBytes());
            return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(directory.toString(), filename)));
        } catch (java.io.IOException e) {
            throw new UnknowException("Unknown error while updating file: " + filename);
        }
    }
}