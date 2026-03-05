package svarcondrej.stream_rec.controller.impl;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = "*")
public class VideoControllerImpl {

    private final String RECORDINGS_DIR = "/app/recordings";

    @GetMapping
    public ResponseEntity<List<String>> listVideos () {
        File folder = new File(RECORDINGS_DIR);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".mp4") || name.endsWith(".webm") || name.endsWith(".mkv"));

        if (files == null) {
            return ResponseEntity.ok(List.of());
        }

        List<String> fileNames = Arrays.stream(files)
                .map(File::getName)
                .collect(Collectors.toList());

        return ResponseEntity.ok(fileNames);
    }

    @GetMapping("/stream/{filename}")
    public ResponseEntity<Resource> streamVideo (@PathVariable String filename) {
        Path filePath = Paths.get(RECORDINGS_DIR).resolve(filename).normalize();

        if (!filePath.startsWith(Paths.get(RECORDINGS_DIR))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        File videoFile = filePath.toFile();
        if (!videoFile.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(videoFile);

        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(resource);
    }
}
