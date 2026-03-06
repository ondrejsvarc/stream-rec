package svarcondrej.stream_rec.controller.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import svarcondrej.stream_rec.controller.ScheduleController;
import svarcondrej.stream_rec.dto.ScheduleRequestDto;
import svarcondrej.stream_rec.enums.RoleEnum;
import svarcondrej.stream_rec.exception.InsufficientPermissionsException;
import svarcondrej.stream_rec.model.RecordingSchedule;
import svarcondrej.stream_rec.model.User;
import svarcondrej.stream_rec.repository.RecordingScheduleRepository;
import svarcondrej.stream_rec.repository.UserRepository;
import svarcondrej.stream_rec.service.ScheduleManagerService;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@CrossOrigin(origins = "*")
public class ScheduleControllerImpl implements ScheduleController {

    private final ScheduleManagerService scheduleManagerService;
    private final RecordingScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    public ScheduleControllerImpl (ScheduleManagerService scheduleManagerService,
                              RecordingScheduleRepository scheduleRepository,
                              UserRepository userRepository) {
        this.scheduleManagerService = scheduleManagerService;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<RecordingSchedule>> getSchedules () {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new InsufficientPermissionsException("Invalid bearer token"));

        if ( currentUser.getRole() == RoleEnum.ADMIN ) {
            return ResponseEntity.ok(scheduleRepository.findAll());
        } else {
            return ResponseEntity.ok(scheduleRepository.findByUserId(currentUser.getId()));
        }
    }

    @PostMapping
    public ResponseEntity<RecordingSchedule> createSchedule (@RequestBody ScheduleRequestDto request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(scheduleManagerService.createSchedule(request, username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecordingSchedule> updateSchedule (@PathVariable String id, @RequestBody ScheduleRequestDto request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(scheduleManagerService.updateSchedule(id, request, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchedule (@PathVariable String id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        scheduleManagerService.deleteSchedule(id, username);
        return ResponseEntity.ok("Schedule deleted successfully");
    }
}
