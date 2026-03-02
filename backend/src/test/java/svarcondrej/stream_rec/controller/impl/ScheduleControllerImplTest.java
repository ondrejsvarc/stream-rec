package svarcondrej.stream_rec.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import svarcondrej.stream_rec.dto.ScheduleRequestDto;
import svarcondrej.stream_rec.enums.JobStatusEnum;
import svarcondrej.stream_rec.model.RecordingSchedule;
import svarcondrej.stream_rec.service.ScheduleManagerService;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class ScheduleControllerImplTest {

    private MockMvc mockMvc;

    @Mock
    private ScheduleManagerService scheduleManagerService;

    @InjectMocks
    private ScheduleControllerImpl scheduleController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(scheduleController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createSchedule_ShouldReturnOk_WhenValidRequest() throws Exception {
        ScheduleRequestDto request = new ScheduleRequestDto();
        request.setStreamUrl("http://example.com/stream");
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));

        RecordingSchedule schedule = new RecordingSchedule();
        schedule.setId("123");
        schedule.setStreamUrl(request.getStreamUrl());
        schedule.setStatus(JobStatusEnum.SCHEDULED);

        when(scheduleManagerService.createSchedule(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(schedule);

        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void createSchedule_ShouldReturnBadRequest_WhenStartTimeIsInThePast() throws Exception {
        ScheduleRequestDto request = new ScheduleRequestDto();
        request.setStreamUrl("http://example.com/stream");
        request.setStartTime(LocalDateTime.now().minusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(1));

        when(scheduleManagerService.createSchedule(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new IllegalArgumentException("Start time cannot be in the past"));

        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSchedule_ShouldReturnBadRequest_WhenEndTimeIsBeforeStartTime() throws Exception {
        ScheduleRequestDto request = new ScheduleRequestDto();
        request.setStreamUrl("http://example.com/stream");
        request.setStartTime(LocalDateTime.now().plusHours(2));
        request.setEndTime(LocalDateTime.now().plusHours(1));

        when(scheduleManagerService.createSchedule(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new IllegalArgumentException("End time must be after start time"));

        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}