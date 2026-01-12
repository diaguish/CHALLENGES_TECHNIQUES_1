package domain.exception;

public class FileNotReadableException extends RuntimeException {
    public FileNotReadableException(String message) {
        super(message);
    }
}
