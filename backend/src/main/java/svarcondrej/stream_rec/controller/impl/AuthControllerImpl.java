package svarcondrej.stream_rec.controller.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import svarcondrej.stream_rec.controller.AuthController;
import svarcondrej.stream_rec.exceptions.IncorrectPasswordException;
import svarcondrej.stream_rec.exceptions.InvalidPasswordException;
import svarcondrej.stream_rec.exceptions.UserNotFoundException;
import svarcondrej.stream_rec.model.User;
import svarcondrej.stream_rec.security.JwtUtil;
import svarcondrej.stream_rec.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthControllerImpl implements AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthControllerImpl (AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username);
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "role", user.getRole().name()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login (@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, credentials.get("password"))
            );

            String token = jwtUtil.generateToken(authentication.getName());
            logger.info("Successful login for user: {}", username);
            return ResponseEntity.ok(Map.of("token", token));

        } catch ( Exception e ) {
            logger.warn("Failed login attempt for username: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword (@RequestBody Map<String, String> request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            userService.changePassword(
                    currentUsername,
                    request.get("oldPassword"),
                    request.get("newPassword")
            );
            return ResponseEntity.ok("Password updated successfully");

        } catch ( InvalidPasswordException | IncorrectPasswordException e ) {
            logger.warn("Password change failed for user '{}': {}", currentUsername, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch ( UserNotFoundException e ) {
            logger.error("User '{}' not found during password change attempt.", currentUsername);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch ( Exception e ) {
            logger.error("Unexpected error during password change for user '{}'", currentUsername, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }
}
