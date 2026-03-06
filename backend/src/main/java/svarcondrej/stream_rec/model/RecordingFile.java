package svarcondrej.stream_rec.model;

import jakarta.persistence.*;
import lombok.Data;
import svarcondrej.stream_rec.enums.PublicityEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recording_file")
@Data
public class RecordingFile {

    @Id
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String filepath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PublicityEnum publicity = PublicityEnum.PRIVATE;

    private LocalDateTime createdAt = LocalDateTime.now();

}
