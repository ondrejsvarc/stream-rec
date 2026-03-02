package svarcondrej.stream_rec.controller.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import svarcondrej.stream_rec.controller.ScheduleController;
import svarcondrej.stream_rec.dto.ScheduleRequestDto;
import svarcondrej.stream_rec.enums.JobStatusEnum;
import svarcondrej.stream_rec.model.RecordingSchedule;
import svarcondrej.stream_rec.service.ScheduleManagerService;

@RestController
@RequestMapping("/api/schedules")
@CrossOrigin(origins = "*")
public class ScheduleControllerImpl implements ScheduleController {
    private final ScheduleManagerService scheduleManagerService;

    public ScheduleControllerImpl(ScheduleManagerService scheduleManagerService) {
        this.scheduleManagerService = scheduleManagerService;
    }

    @PostMapping
    public ResponseEntity<RecordingSchedule> createSchedule(@RequestBody ScheduleRequestDto request) {
        RecordingSchedule savedSchedule;
        try {
            savedSchedule = scheduleManagerService.createSchedule(
                    request.getStreamUrl(),
                    request.getStartTime(),
                    request.getEndTime()
            );
        } catch (IllegalArgumentException e) {
            RecordingSchedule failedSchedule = new RecordingSchedule();
            failedSchedule.setStreamUrl(request.getStreamUrl());
            failedSchedule.setStatus(JobStatusEnum.FAILED);
            return ResponseEntity.badRequest().body(failedSchedule);
        }
        return ResponseEntity.ok(savedSchedule);
    }
}
