package svarcondrej.stream_rec.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ScheduleRequestDto {
    private String name;
    private String streamUrl;
    private LocalDateTime startTime;
    private Integer duration;
    private String repetition;
    private Integer customRepetitionDays;
    private String codec;
    private Integer maxTranscodeBitrate;
}
