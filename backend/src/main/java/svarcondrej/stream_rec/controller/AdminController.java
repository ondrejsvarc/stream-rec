package svarcondrej.stream_rec.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import svarcondrej.stream_rec.model.User;

import java.util.List;
import java.util.Map;

public interface AdminController {

    public ResponseEntity<List<User>> getAllUsers ();

    @PostMapping
    public ResponseEntity<User> createUser (@RequestBody Map<String, String> request);

    public ResponseEntity<?> deleteUser (@PathVariable String id);
}
