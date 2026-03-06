package svarcondrej.stream_rec.controller;

import org.springframework.http.ResponseEntity;
import svarcondrej.stream_rec.model.RecordingJob;

import java.util.List;

public interface JobController {
    public ResponseEntity<List<RecordingJob>> getJobs ();
}
