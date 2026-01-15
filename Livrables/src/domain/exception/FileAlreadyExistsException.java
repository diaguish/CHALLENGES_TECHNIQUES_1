package domain.exception;

/**
 * Exception thrown when attempting to create a file that already exists.
 */
public class FileAlreadyExistsException extends RuntimeException {
    /**
     * Constructs a FileAlreadyExistsException with the specified message.
     * 
     * @param message the error message
     */
    public FileAlreadyExistsException(String message) {
        super(message);
    }
}
