package domain.exception;

/**
 * Exception thrown when a cryptography operation fails.
 */
public class CryptoException extends RuntimeException {
    /**
     * Constructs a CryptoException with the specified message.
     * 
     * @param message the error message
     */
    public CryptoException(String message) {
        super(message);
    }
}
