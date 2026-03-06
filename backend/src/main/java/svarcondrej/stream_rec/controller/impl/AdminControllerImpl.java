package svarcondrej.stream_rec.controller.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import svarcondrej.stream_rec.controller.AdminController;
import svarcondrej.stream_rec.enums.RoleEnum;
import svarcondrej.stream_rec.model.User;
import svarcondrej.stream_rec.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
public class AdminControllerImpl implements AdminController {

    private final UserService userService;

    public AdminControllerImpl (UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers () {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<User> createUser (@RequestBody Map<String, String> request) {
        User newUser = userService.createUser(
                request.get("username"),
                request.get("password"),
                RoleEnum.valueOf(request.get("role").toUpperCase())
        );
        return ResponseEntity.ok(newUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser (@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted");
    }
}
