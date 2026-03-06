package svarcondrej.stream_rec.model;

import jakarta.persistence.*;
import lombok.Data;
import svarcondrej.stream_rec.enums.JobStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recording_job")
@Data
public class RecordingJob {

    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private RecordingSchedule schedule;

    @Column(nullable = false)
    private LocalDateTime actualStartTime;

    private LocalDateTime actualEndTime;

    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatusEnum status = JobStatusEnum.RECORDING;

}