package domain.exception;

/**
 * Exception thrown when a file cannot be read due to permission or format issues.
 */
public class FileNotReadableException extends RuntimeException {
    /**
     * Constructs a FileNotReadableException with the specified message.
     * 
     * @param message the error message
     */
    public FileNotReadableException(String message) {
        super(message);
    }
}
