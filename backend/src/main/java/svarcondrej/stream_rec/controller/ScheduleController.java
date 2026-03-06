package svarcondrej.stream_rec.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import svarcondrej.stream_rec.dto.ScheduleRequestDto;
import svarcondrej.stream_rec.model.RecordingSchedule;

import java.util.List;

public interface ScheduleController {

    public ResponseEntity<List<RecordingSchedule>> getSchedules ();

    public ResponseEntity<RecordingSchedule> createSchedule (@RequestBody ScheduleRequestDto request);

    public ResponseEntity<RecordingSchedule> updateSchedule (@PathVariable String id, @RequestBody ScheduleRequestDto request);

    public ResponseEntity<?> deleteSchedule (@PathVariable String id);

}
