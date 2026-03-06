package svarcondrej.stream_rec.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import svarcondrej.stream_rec.model.RecordingFile;

import java.util.List;

public interface RecordingFileRepository extends JpaRepository<RecordingFile, String> {

    @Query("SELECT f FROM RecordingFile f WHERE f.owner.id = :userId OR f.publicity = 'PUBLIC' ORDER BY f.createdAt DESC")
    List<RecordingFile> findVisibleFilesForUser (@Param("userId") String userId);

    List<RecordingFile> findAllByOrderByCreatedAtDesc ();
}
