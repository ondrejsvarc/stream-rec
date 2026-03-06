package svarcondrej.stream_rec.service;

import svarcondrej.stream_rec.enums.RoleEnum;
import svarcondrej.stream_rec.model.User;

import java.util.List;

public interface UserService {

    public User createUser (String username, String rawPassword);

    public User createUser (String username, String rawPassword, RoleEnum role);

    public List<User> getAllUsers();

    public void deleteUser (String id);

    public User findByUsername (String username);

    public void changePassword (String username, String oldPassword, String newPassword);
}
