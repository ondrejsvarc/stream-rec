package svarcondrej.stream_rec.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

public interface VideoController {

    public ResponseEntity<Resource> streamVideo(@PathVariable String jobId);
}
