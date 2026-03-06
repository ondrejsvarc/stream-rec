package svarcondrej.stream_rec.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import svarcondrej.stream_rec.enums.JobStatusEnum;
import svarcondrej.stream_rec.model.RecordingJob;

import java.util.List;

public interface RecordingJobRepository extends JpaRepository<RecordingJob, String> {
    List<RecordingJob> findByScheduleId(String scheduleId);
    List<RecordingJob> findByStatus(JobStatusEnum status);
}
