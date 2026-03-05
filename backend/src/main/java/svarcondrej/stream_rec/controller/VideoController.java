package svarcondrej.stream_rec.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface VideoController {

    public ResponseEntity<List<String>> listVideos ();

    public ResponseEntity<Resource> streamVideo (@PathVariable String filename);
}
