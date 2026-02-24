package svarcondrej.stream_rec.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class StatusDto {
    Timestamp timestamp;
    String status;
    String message;
}
