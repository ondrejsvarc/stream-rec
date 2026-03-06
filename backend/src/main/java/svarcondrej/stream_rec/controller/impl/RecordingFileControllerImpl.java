package svarcondrej.stream_rec.controller.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import svarcondrej.stream_rec.controller.RecordingFileController;
import svarcondrej.stream_rec.enums.PublicityEnum;
import svarcondrej.stream_rec.enums.RoleEnum;
import svarcondrej.stream_rec.exception.InsufficientPermissionsException;
import svarcondrej.stream_rec.model.RecordingFile;
import svarcondrej.stream_rec.model.User;
import svarcondrej.stream_rec.repository.RecordingFileRepository;
import svarcondrej.stream_rec.repository.UserRepository;

import java.io.File;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class RecordingFileControllerImpl implements RecordingFileController {

    private final RecordingFileRepository fileRepository;
    private final UserRepository userRepository;

    public RecordingFileControllerImpl (RecordingFileRepository fileRepository, UserRepository userRepository) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser () {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow(() -> new InsufficientPermissionsException("Invalid bearer token"));
    }

    @GetMapping
    public ResponseEntity<List<RecordingFile>> getLibrary () {
        User currentUser = getCurrentUser();
        if ( currentUser.getRole() == RoleEnum.ADMIN ) {
            return ResponseEntity.ok(fileRepository.findAllByOrderByCreatedAtDesc());
        }
        return ResponseEntity.ok(fileRepository.findVisibleFilesForUser(currentUser.getId()));
    }

    @PutMapping("/{id}/publicity")
    public ResponseEntity<?> updatePublicity (@PathVariable String id, @RequestBody Map<String, String> request) {
        RecordingFile file = fileRepository.findById(id).orElseThrow();
        User currentUser = getCurrentUser();

        if ( currentUser.getRole() != RoleEnum.ADMIN && !file.getOwner().getId().equals(currentUser.getId()) ) {
            throw new InsufficientPermissionsException("Insufficient permissions for this action");
        }

        file.setPublicity(PublicityEnum.valueOf(request.get("publicity").toUpperCase()));
        fileRepository.save(file);
        return ResponseEntity.ok("Publicity updated");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile (@PathVariable String id) {
        RecordingFile file = fileRepository.findById(id).orElseThrow();
        User currentUser = getCurrentUser();

        if ( currentUser.getRole() != RoleEnum.ADMIN && !file.getOwner().getId().equals(currentUser.getId()) ) {
            throw new InsufficientPermissionsException("Insufficient permissions for this action");
        }

        File diskFile = new File(file.getFilepath());
        if ( diskFile.exists() ) {
            diskFile.delete();
        }

        fileRepository.delete(file);
        return ResponseEntity.ok("File deleted successfully");
    }
}
