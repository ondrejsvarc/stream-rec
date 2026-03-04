package svarcondrej.stream_rec.service.impl;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import svarcondrej.stream_rec.exceptions.IncorrectPasswordException;
import svarcondrej.stream_rec.exceptions.InvalidPasswordException;
import svarcondrej.stream_rec.exceptions.UserAlreadyExistsException;
import svarcondrej.stream_rec.exceptions.UserNotFoundException;
import svarcondrej.stream_rec.model.User;
import svarcondrej.stream_rec.repository.UserRepository;
import svarcondrej.stream_rec.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl (UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void seedInitialUser () {
        if ( userRepository.count() == 0 ) {
            logger.info("No users found in database. Creating default 'admin' user.");
            createUser("admin", "p4sswOrd");
            logger.warn("IMPORTANT: Please log in and change the default 'admin' password immediately!");
        }
    }

    public User createUser (String username, String rawPassword) {
        if ( userRepository.findByUsername(username).isPresent() ) {
            throw new UserAlreadyExistsException("Username '" + username + "' already exists.");
        }

        validatePasswordComplexity(rawPassword);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    public void changePassword (String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        if ( !passwordEncoder.matches(oldPassword, user.getPassword()) ) {
            throw new IncorrectPasswordException("The current password provided is incorrect.");
        }

        validatePasswordComplexity(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password changed successfully for user: {}", username);
    }

    private void validatePasswordComplexity (String password) {
        int matchCount = 0;

        if ( password == null || password.length() < 8 ) {
            throw new InvalidPasswordException("Password must be at least 8 characters long.");
        }

        if ( password.matches(".*[a-z].*") ) matchCount++;
        if ( password.matches(".*[A-Z].*") ) matchCount++;
        if ( password.matches(".*\\d.*") ) matchCount++;
        if ( password.matches(".*[^a-zA-Z0-9].*") ) matchCount++;

        if ( matchCount < 3 ) {
            throw new InvalidPasswordException("Password must contain at least 3 of the following: lowercase letters, uppercase letters, numbers, and special characters.");
        }
    }
}