package domain.exception;

/**
 * Exception thrown when an operation targets a file that does not exist.
 */
public class FileNotFoundException extends RuntimeException {
    /**
     * Constructs a FileNotFoundException with the specified message.
     * 
     * @param message the error message
     */
    public FileNotFoundException(String message) {
        super(message);
    }
}
