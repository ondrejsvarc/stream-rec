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

    @ExceptionHandler({
            UserNotFoundException.class,
            ScheduleNotFoundException.class,
            RecordingJobNotFoundException.class
    })
    public ResponseEntity<String> handleNotFoundExceptions (UserNotFoundException ex) {
        logger.warn("Not Found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler({
            InsufficientPermissionsException.class
    })
    public ResponseEntity<String> handleForbiddenExceptions (InsufficientPermissionsException ex) {
        logger.warn("Forbidden: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler({
            Exception.class
    })
    public ResponseEntity<String> handleGenericException (Exception ex) {
        logger.error("An unexpected error occurred", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected internal server error occurred.");
    }

    @ExceptionHandler({
            AuthenticationException.class
    })
    public ResponseEntity<String> handleAuthenticationExceptions(AuthenticationException ex) {
        logger.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
    }
}
