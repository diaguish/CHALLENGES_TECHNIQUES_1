package domain.exception;

/**
 * Exception thrown when a file access operation fails.
 */
public class FileAccessException extends RuntimeException {
    /**
     * Constructs a FileAccessException with the specified message.
     * 
     * @param message the error message
     */
    public FileAccessException(String message) {
        super(message);
    }
}
