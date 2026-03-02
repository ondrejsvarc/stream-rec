package svarcondrej.stream_rec.controller.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import svarcondrej.stream_rec.controller.RecordingController;
import svarcondrej.stream_rec.dto.RecStartDto;
import svarcondrej.stream_rec.dto.RecStopDto;
import svarcondrej.stream_rec.exceptions.RecordingJobNotFoundException;
import svarcondrej.stream_rec.service.RecordingService;

import java.util.UUID;

@RestController
@RequestMapping("/api/recordings")
@CrossOrigin(origins = "*")
public class RecordingControllerImpl implements RecordingController {

    private final RecordingService recordingService;

    public RecordingControllerImpl(RecordingService recordingService) {
        this.recordingService = recordingService;
    }

    @PostMapping("/start")
    public ResponseEntity<RecStartDto> startRecording(@RequestParam String url) {
        String jobId = UUID.randomUUID().toString();

        try {
            recordingService.startRecording(url, jobId);
        } catch (Exception e) {
            RecStartDto dto = new RecStartDto();
            dto.setTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));
            dto.setJobId("null");
            dto.setStatus("Failed to start recording.");
            return ResponseEntity.status(500).body(dto);
        }

        RecStartDto dto = new RecStartDto();
        dto.setTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));
        dto.setJobId(jobId);
        dto.setStatus("Recording started successfully.");

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/stop/{jobId}")
    public ResponseEntity<RecStopDto> stopRecording(@PathVariable String jobId) {
        try {
            recordingService.stopRecording(jobId);
        } catch (RecordingJobNotFoundException e) {
            RecStopDto errorDto = new RecStopDto();
            errorDto.setTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));
            errorDto.setJobId(jobId);
            errorDto.setStatus("Recording job not found.");
            return ResponseEntity.status(404).body(errorDto);
        }
        RecStopDto dto = new RecStopDto();
        dto.setTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));
        dto.setJobId(jobId);
        dto.setStatus("Requested stop");
        return ResponseEntity.ok(dto);
    }
}
