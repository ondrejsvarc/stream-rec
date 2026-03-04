package svarcondrej.stream_rec.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

public interface AuthController {

    public ResponseEntity<?> login (@RequestBody Map<String, String> credentials);

    public ResponseEntity<?> changePassword (@RequestBody Map<String, String> request);
}
