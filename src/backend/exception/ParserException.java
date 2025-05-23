package backend.exception;

/**
 * Exception thrown when parsing fails.
 */

public class ParserException extends RuntimeException {
    public ParserException(String message) {
        super(message);
    }
    
    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }
}