package svarcondrej.stream_rec.service;

import svarcondrej.stream_rec.enums.Role;
import svarcondrej.stream_rec.exceptions.UserNotFoundException;
import svarcondrej.stream_rec.model.User;

import java.util.List;

public interface UserService {

    public User createUser (String username, String rawPassword);

    public User createUser (String username, String rawPassword, Role role);

    public List<User> getAllUsers();

    public void deleteUser (String id);

    public User findByUsername (String username);

    public void changePassword (String username, String oldPassword, String newPassword);
}
