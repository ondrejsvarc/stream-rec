package svarcondrej.stream_rec.model;

import jakarta.persistence.*;
import lombok.Data;
import svarcondrej.stream_rec.enums.JobStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class RecordingSchedule {

    @Id
    private String id = UUID.randomUUID().toString();

    private String streamUrl;

    // ToDo - Add more fields: quality, codec, repeating job, ...
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private JobStatusEnum status;
}
