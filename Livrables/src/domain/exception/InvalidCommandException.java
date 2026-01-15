package domain.exception;

/**
 * Exception thrown when an invalid command is provided.
 */
public class InvalidCommandException extends RuntimeException {
    /**
     * Constructs an InvalidCommandException with the specified message.
     * 
     * @param message the error message
     */
    public InvalidCommandException(String message) {
        super(message);
    }
}
