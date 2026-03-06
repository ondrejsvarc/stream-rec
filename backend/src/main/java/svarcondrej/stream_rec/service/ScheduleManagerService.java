package svarcondrej.stream_rec.service;

import org.quartz.Scheduler;
import svarcondrej.stream_rec.dto.ScheduleRequestDto;
import svarcondrej.stream_rec.model.RecordingSchedule;
import svarcondrej.stream_rec.repository.RecordingScheduleRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleManagerService {

    public RecordingSchedule createSchedule (ScheduleRequestDto request, String username);

    public RecordingSchedule updateSchedule (String scheduleId, ScheduleRequestDto request, String username);

    public void deleteSchedule (String scheduleId, String username);

    public void queueQuartzJobs (RecordingSchedule schedule);
}
