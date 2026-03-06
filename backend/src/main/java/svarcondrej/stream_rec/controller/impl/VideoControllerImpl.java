package svarcondrej.stream_rec.controller.impl;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import svarcondrej.stream_rec.controller.VideoController;
import svarcondrej.stream_rec.model.RecordingJob;
import svarcondrej.stream_rec.repository.RecordingJobRepository;

import java.io.File;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = "*")
public class VideoControllerImpl implements VideoController {

    private final RecordingJobRepository jobRepository;

    public VideoControllerImpl (RecordingJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @GetMapping("/stream/{jobId}")
    public ResponseEntity<Resource> streamVideo (@PathVariable String jobId) {

        RecordingJob job = jobRepository.findById(jobId).orElse(null);
        if ( job == null || job.getFilePath() == null ) {
            return ResponseEntity.notFound().build();
        }

        File videoFile = new File(job.getFilePath());

        if ( !videoFile.exists() ) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(videoFile);

        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(resource);
    }
}
