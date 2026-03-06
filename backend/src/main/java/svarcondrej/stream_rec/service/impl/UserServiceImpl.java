package svarcondrej.stream_rec.service.impl;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import svarcondrej.stream_rec.enums.RoleEnum;
import svarcondrej.stream_rec.exception.IncorrectPasswordException;
import svarcondrej.stream_rec.exception.InvalidPasswordException;
import svarcondrej.stream_rec.exception.UserAlreadyExistsException;
import svarcondrej.stream_rec.exception.UserNotFoundException;
import svarcondrej.stream_rec.model.User;
import svarcondrej.stream_rec.repository.UserRepository;
import svarcondrej.stream_rec.service.UserService;
import svarcondrej.stream_rec.util.DatabaseLock;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final String ALL_CHARS = LOWERCASE + UPPERCASE + NUMBERS;
    private static final SecureRandom random = new SecureRandom();

    public UserServiceImpl (UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private String generateSecurePassword (int length) {
        List<Character> passwordChars = new ArrayList<>(length);

        passwordChars.add(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        passwordChars.add(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        passwordChars.add(NUMBERS.charAt(random.nextInt(NUMBERS.length())));

        for ( int i = 3; i < length; i++ ) {
            passwordChars.add(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        Collections.shuffle(passwordChars, random);

        StringBuilder password = new StringBuilder(length);
        for ( Character c : passwordChars ) {
            password.append(c);
        }

        return password.toString();
    }

    @PostConstruct
    public void seedInitialUser () {
        if ( userRepository.count() == 0 ) {
            logger.warn("No users found in database. Creating default 'admin' user.");
            String defaultUsername = "admin";
            String defaultPassword = generateSecurePassword(8);
            createUser(defaultUsername, defaultPassword, RoleEnum.ADMIN);
            logger.warn("\n----------------------------------------\n|     Generated password: {}     |\n----------------------------------------\n", defaultPassword);
            logger.warn("IMPORTANT: Please log in and change the default 'admin' password immediately!");
        }
    }

    public User createUser (String username, String rawPassword, RoleEnum role) {
        if ( userRepository.findByUsername(username).isPresent() ) {
            throw new UserAlreadyExistsException("Username '" + username + "' already exists.");
        }
        validatePasswordComplexity(rawPassword);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);

        DatabaseLock.WRITE_LOCK.lock();
        try {
            return userRepository.save(user);
        } finally {
            DatabaseLock.WRITE_LOCK.unlock();
        }
    }

    public User createUser (String username, String rawPassword) {
        return createUser(username, rawPassword, RoleEnum.USER);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser (String id) {
        DatabaseLock.WRITE_LOCK.lock();
        try {
            userRepository.deleteById(id);
        } finally {
            DatabaseLock.WRITE_LOCK.unlock();
        }
    }

    public User findByUsername (String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    public void changePassword (String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        if ( !passwordEncoder.matches(oldPassword, user.getPassword()) ) {
            throw new IncorrectPasswordException("The current password provided is incorrect.");
        }

        validatePasswordComplexity(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));

        DatabaseLock.WRITE_LOCK.lock();
        try {
            userRepository.save(user);
        } finally {
            DatabaseLock.WRITE_LOCK.unlock();
        }

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