package svarcondrej.stream_rec.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import svarcondrej.stream_rec.enums.JobStatusEnum;
import svarcondrej.stream_rec.exception.RecordingJobNotFoundException;
import svarcondrej.stream_rec.model.RecordingSchedule;
import svarcondrej.stream_rec.repository.RecordingScheduleRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecordingServiceImplTest {

    @Mock
    private RecordingScheduleRepository repository;

    @Mock
    private Process mockProcess;

    @InjectMocks
    private RecordingServiceImpl recordingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void stopRecording_ShouldThrowException_WhenJobIdNotFound() {
        assertThrows(RecordingJobNotFoundException.class, () -> {
            recordingService.stopRecording("non-existent-job");
        });
    }

    @Test
    void stopRecording_ShouldAttemptGracefulShutdown_WhenProcessIsAlive() throws Exception {
        String jobId = "active-job";

        when(mockProcess.isAlive()).thenReturn(true);
        when(mockProcess.pid()).thenReturn(12345L);
        when(mockProcess.waitFor(15, TimeUnit.SECONDS)).thenReturn(true);

        Map<String, Process> map = new ConcurrentHashMap<>();
        map.put(jobId, mockProcess);
        ReflectionTestUtils.setField(recordingService, "activeRecordings", map);

        RecordingSchedule mockSchedule = new RecordingSchedule();
        mockSchedule.setId(jobId);
        mockSchedule.setStatus(JobStatusEnum.RECORDING);
        when(repository.findById(jobId)).thenReturn(Optional.of(mockSchedule));

        try {
            recordingService.stopRecording(jobId);
        } catch (Exception ignored) {}

        verify(repository, times(1)).findById(jobId);
        verify(repository, times(1)).save(mockSchedule);
        assertEquals(JobStatusEnum.COMPLETED, mockSchedule.getStatus());
    }
}