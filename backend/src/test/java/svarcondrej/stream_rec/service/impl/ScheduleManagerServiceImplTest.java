package svarcondrej.stream_rec.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import svarcondrej.stream_rec.enums.JobStatusEnum;
import svarcondrej.stream_rec.model.RecordingSchedule;
import svarcondrej.stream_rec.repository.RecordingScheduleRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ScheduleManagerServiceImplTest {

    @Mock
    private Scheduler scheduler;

    @Mock
    private RecordingScheduleRepository repository;

    @InjectMocks
    private ScheduleManagerServiceImpl scheduleManagerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createSchedule_ShouldSaveScheduleAndTriggerQuartzJobs() throws SchedulerException {
        String streamUrl = "http://example.com/stream";
        LocalDateTime startTime = LocalDateTime.now().plusMinutes(5);
        LocalDateTime endTime = LocalDateTime.now().plusMinutes(65);

        RecordingSchedule mockSchedule = new RecordingSchedule();
        mockSchedule.setId("mock-uuid");
        mockSchedule.setStreamUrl(streamUrl);
        mockSchedule.setStartTime(startTime);
        mockSchedule.setEndTime(endTime);
        mockSchedule.setStatus(JobStatusEnum.SCHEDULED);

        when(repository.save(any(RecordingSchedule.class))).thenReturn(mockSchedule);

        RecordingSchedule result = scheduleManagerService.createSchedule(streamUrl, startTime, endTime);

        assertNotNull(result);
        assertEquals("mock-uuid", result.getId());
        assertEquals(JobStatusEnum.SCHEDULED, result.getStatus());

        ArgumentCaptor<RecordingSchedule> scheduleCaptor = ArgumentCaptor.forClass(RecordingSchedule.class);
        verify(repository, times(1)).save(scheduleCaptor.capture());

        RecordingSchedule capturedSchedule = scheduleCaptor.getValue();
        assertEquals(streamUrl, capturedSchedule.getStreamUrl());

        verify(scheduler, times(2)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    void createSchedule_ShouldThrowIllegalArgumentException_WhenStartTimeIsInThePast() {
        String streamUrl = "http://example.com/stream";
        LocalDateTime pastStartTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);

        assertThrows(IllegalArgumentException.class, () -> {
            scheduleManagerService.createSchedule(streamUrl, pastStartTime, endTime);
        }, "Should throw an exception if start time is in the past");
    }

    @Test
    void createSchedule_ShouldThrowIllegalArgumentException_WhenEndTimeIsBeforeStartTime() {
        String streamUrl = "http://example.com/stream";
        LocalDateTime startTime = LocalDateTime.now().plusHours(2);
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);

        assertThrows(IllegalArgumentException.class, () -> {
            scheduleManagerService.createSchedule(streamUrl, startTime, endTime);
        }, "Should throw an exception if end time is before start time");
    }
}