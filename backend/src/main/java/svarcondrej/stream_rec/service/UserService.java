package svarcondrej.stream_rec.service;

import svarcondrej.stream_rec.model.User;

public interface UserService {

    public User createUser (String username, String rawPassword);

    public void changePassword (String username, String oldPassword, String newPassword);
}
