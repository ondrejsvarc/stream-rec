package svarcondrej.stream_rec.controller.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import svarcondrej.stream_rec.dto.RecStartDto;
import svarcondrej.stream_rec.dto.RecStopDto;
import svarcondrej.stream_rec.exception.RecordingJobNotFoundException;
import svarcondrej.stream_rec.service.RecordingService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RecordingControllerImplTest {

    @Mock
    private RecordingService recordingService;

    @InjectMocks
    private RecordingControllerImpl recordingController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void startRecording_ShouldReturnOk_WhenServiceSucceeds() {
        String url = "http://example.com/stream";
        doNothing().when(recordingService).startRecording(eq(url), anyString());

        ResponseEntity<RecStartDto> response = recordingController.startRecording(url);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Recording started successfully.", response.getBody().getStatus());
        assertNotNull(response.getBody().getJobId());

        verify(recordingService, times(1)).startRecording(eq(url), anyString());
    }

    @Test
    void stopRecording_ShouldReturnOk_WhenJobExists() {
        String jobId = "test-job-id";
        doNothing().when(recordingService).stopRecording(jobId);

        ResponseEntity<RecStopDto> response = recordingController.stopRecording(jobId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Requested stop", response.getBody().getStatus());
        assertEquals(jobId, response.getBody().getJobId());

        verify(recordingService, times(1)).stopRecording(jobId);
    }

    @Test
    void stopRecording_ShouldReturn404_WhenJobNotFound() {
        String jobId = "unknown-job-id";
        doThrow(new RecordingJobNotFoundException("Not found")).when(recordingService).stopRecording(jobId);

        ResponseEntity<RecStopDto> response = recordingController.stopRecording(jobId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Recording job not found.", response.getBody().getStatus());
        assertEquals(jobId, response.getBody().getJobId());
    }
}