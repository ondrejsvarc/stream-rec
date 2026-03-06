package svarcondrej.stream_rec.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import svarcondrej.stream_rec.model.RecordingFile;

import java.util.List;
import java.util.Map;

public interface RecordingFileController {

    public ResponseEntity<List<RecordingFile>> getLibrary ();

    public ResponseEntity<?> updatePublicity (@PathVariable String id, @RequestBody Map<String, String> request);

    public ResponseEntity<?> deleteFile (@PathVariable String id);
}
