package svarcondrej.stream_rec.controller.impl;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import svarcondrej.stream_rec.controller.VideoController;
import svarcondrej.stream_rec.enums.PublicityEnum;
import svarcondrej.stream_rec.enums.RoleEnum;
import svarcondrej.stream_rec.model.RecordingFile;
import svarcondrej.stream_rec.model.User;
import svarcondrej.stream_rec.repository.RecordingFileRepository;
import svarcondrej.stream_rec.repository.UserRepository;

import java.io.File;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = "*")
public class VideoControllerImpl implements VideoController {

    private final RecordingFileRepository fileRepository;
    private final UserRepository userRepository;

    public VideoControllerImpl (RecordingFileRepository fileRepository, UserRepository userRepository) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/stream/{fileId}")
    public ResponseEntity<Resource> streamVideo (@PathVariable String fileId) {
        RecordingFile recordingFile = fileRepository.findById(fileId).orElse(null);
        if ( recordingFile == null ) return ResponseEntity.notFound().build();

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);

        if ( currentUser != null ) {
            boolean isAdmin = currentUser.getRole() == RoleEnum.ADMIN;
            boolean isOwner = recordingFile.getOwner().getId().equals(currentUser.getId());

            if ( !isAdmin && !isOwner && recordingFile.getPublicity() == PublicityEnum.PRIVATE ) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        File videoFile = new File(recordingFile.getFilepath());
        if ( !videoFile.exists() ) return ResponseEntity.notFound().build();

        Resource resource = new FileSystemResource(videoFile);
        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(resource);
    }
}
