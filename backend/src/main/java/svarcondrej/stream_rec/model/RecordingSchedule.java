package svarcondrej.stream_rec.model;

import jakarta.persistence.*;
import lombok.Data;
import svarcondrej.stream_rec.enums.CodecEnum;
import svarcondrej.stream_rec.enums.ScheduleRepetitionEnum;
import svarcondrej.stream_rec.enums.ScheduleStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recording_schedule")
@Data
public class RecordingSchedule {

    @Id
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String streamUrl;

    private LocalDateTime startTime;

    private Integer duration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleRepetitionEnum repetition = ScheduleRepetitionEnum.NONE;

    private Integer customRepetitionDays;

    @Enumerated(EnumType.STRING)
    private CodecEnum codec = CodecEnum.NONE;

    private Integer maxTranscodeBitrate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleStatusEnum status = ScheduleStatusEnum.SCHEDULED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
