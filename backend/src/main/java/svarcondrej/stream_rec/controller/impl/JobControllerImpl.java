package svarcondrej.stream_rec.controller.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import svarcondrej.stream_rec.controller.JobController;
import svarcondrej.stream_rec.enums.RoleEnum;
import svarcondrej.stream_rec.model.RecordingJob;
import svarcondrej.stream_rec.model.User;
import svarcondrej.stream_rec.repository.RecordingJobRepository;
import svarcondrej.stream_rec.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobControllerImpl implements JobController {

    private final RecordingJobRepository jobRepository;
    private final UserRepository userRepository;

    public JobControllerImpl (RecordingJobRepository jobRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<RecordingJob>> getJobs () {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new InsufficientAuthenticationException("Invalid bearer token"));

        List<RecordingJob> allJobs = jobRepository.findAll();

        if ( currentUser.getRole() == RoleEnum.ADMIN ) {
            return ResponseEntity.ok(allJobs);
        } else {
            List<RecordingJob> userJobs = allJobs.stream()
                    .filter(job -> job.getSchedule().getUser().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userJobs);
        }
    }
}
