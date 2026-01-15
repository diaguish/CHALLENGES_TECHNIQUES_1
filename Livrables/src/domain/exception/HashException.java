package domain.exception;

/**
 * Exception thrown when a hashing operation fails.
 */
public class HashException extends RuntimeException {
    /**
     * Constructs a HashException with the specified message.
     * 
     * @param message the error message
     */
    public HashException(String message) {
        super(message);
    }
}
