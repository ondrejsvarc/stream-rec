package svarcondrej.stream_rec.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import svarcondrej.stream_rec.enums.JobStatusEnum;
import svarcondrej.stream_rec.model.RecordingSchedule;

import java.util.List;

@Repository
public interface RecordingScheduleRepository extends JpaRepository<RecordingSchedule, String> {
    List<RecordingSchedule> findByStatus(JobStatusEnum status);
}
