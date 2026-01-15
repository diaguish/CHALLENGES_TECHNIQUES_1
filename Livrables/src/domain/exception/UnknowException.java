package domain.exception;

/**
 * Exception thrown when an unknown error occurs.
 */
public class UnknowException extends RuntimeException {
    /**
     * Constructs an UnknowException with the specified message.
     * 
     * @param message the error message
     */
    public UnknowException(String message) {
        super(message);
    }
}
