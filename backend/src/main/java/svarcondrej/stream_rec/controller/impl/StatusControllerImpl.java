package svarcondrej.stream_rec.controller.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import svarcondrej.stream_rec.controller.StatusController;
import svarcondrej.stream_rec.dto.StatusDto;
import java.sql.Timestamp;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class StatusControllerImpl implements StatusController {

    @GetMapping("/status")
    public ResponseEntity<StatusDto> getStatus() {
        StatusDto dto = new StatusDto();
        dto.setTimestamp(new Timestamp(System.currentTimeMillis()));
        dto.setStatus("online");
        dto.setMessage("Stream Recorder Backend is running!");
        return ResponseEntity.ok(dto);
    }
}
