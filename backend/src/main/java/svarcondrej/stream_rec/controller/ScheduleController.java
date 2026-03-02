package svarcondrej.stream_rec.controller;

import org.springframework.http.ResponseEntity;
import svarcondrej.stream_rec.dto.ScheduleRequestDto;
import svarcondrej.stream_rec.model.RecordingSchedule;

public interface ScheduleController {
    public ResponseEntity<RecordingSchedule> createSchedule(ScheduleRequestDto request);
}
