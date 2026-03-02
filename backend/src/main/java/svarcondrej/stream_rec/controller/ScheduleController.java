package svarcondrej.stream_rec.controller;

import org.springframework.http.ResponseEntity;
import svarcondrej.stream_rec.dto.ScheduleRequestDto;
import svarcondrej.stream_rec.model.RecordingSchedule;

import java.util.List;

public interface ScheduleController {
    public ResponseEntity<List<RecordingSchedule>> getAllSchedules();
    public ResponseEntity<RecordingSchedule> createSchedule(ScheduleRequestDto request);
}
