package svarcondrej.stream_rec.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Catches all exceptions related to bad user input and returns a 400 Bad Request.
     */
    @ExceptionHandler({
            InvalidPasswordException.class,
            IncorrectPasswordException.class,
            UserAlreadyExistsException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<String> handleBadRequestExceptions (RuntimeException ex) {
        logger.warn("Bad Request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Catches exceptions where a requested resource is missing and returns a 404 Not Found.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleNotFoundExceptions (UserNotFoundException ex) {
        logger.warn("Not Found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Optional: A catch-all for any other unexpected server crashes (500 Internal Server Error).
     * This prevents Spring from sending massive stack traces to the frontend.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException (Exception ex) {
        logger.error("An unexpected error occurred", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected internal server error occurred.");
    }

    /**
     * Catches Spring Security login failures and returns a 401 Unauthorized.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationExceptions(AuthenticationException ex) {
        logger.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
    }
}
