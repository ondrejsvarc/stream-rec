package svarcondrej.stream_rec.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ScheduleRequestDto {
    private String streamUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
