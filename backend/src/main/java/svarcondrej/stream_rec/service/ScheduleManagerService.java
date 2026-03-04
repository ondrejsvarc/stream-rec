package svarcondrej.stream_rec.service;

import org.quartz.Scheduler;
import svarcondrej.stream_rec.model.RecordingSchedule;
import svarcondrej.stream_rec.repository.RecordingScheduleRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleManagerService {

    public List<RecordingSchedule> getAllSchedules();

    public RecordingSchedule createSchedule(String streamUrl, LocalDateTime startTime, LocalDateTime endTime);

    public void syncSchedulesOnStartup();
}
