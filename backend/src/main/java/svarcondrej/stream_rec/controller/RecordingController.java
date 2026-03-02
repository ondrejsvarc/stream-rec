package svarcondrej.stream_rec.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import svarcondrej.stream_rec.dto.RecStartDto;
import svarcondrej.stream_rec.dto.RecStopDto;

/**
 * REST controller interface for managing stream recording jobs.
 */
public interface RecordingController {

    /**
     * Initializes and starts a new recording job for the provided stream URL.
     *
     * @param url the target URL of the media stream to record, typically passed as a query parameter
     * @return a {@link ResponseEntity} containing a {@link RecStartDto} with:
     * <ul>
     * <li><b>jobId</b>: The generated unique identifier for the new recording job.</li>
     * <li><b>status</b>: A message indicating successful initialization or failure.</li>
     * <li><b>timestamp</b>: The server time the request was processed.</li>
     * </ul>
     * Returns an HTTP 200 (OK) on success, or HTTP 500 (Internal Server Error) if the recording process fails to start.
     */
    public ResponseEntity<RecStartDto> startRecording(@RequestParam String url);

    /**
     * Requests the termination of an actively running recording job.
     *
     * @param jobId the unique identifier of the active recording job to stop, passed as a path variable
     * @return a {@link ResponseEntity} containing a {@link RecStopDto} with:
     * <ul>
     * <li><b>jobId</b>: The identifier of the job that was requested to stop.</li>
     * <li><b>status</b>: A confirmation message or an error description.</li>
     * <li><b>timestamp</b>: The server time the request was processed.</li>
     * </ul>
     * Returns HTTP 200 (OK) if the stop request was issued, or HTTP 404 (Not Found) if the specified job does not exist.
     */
    public ResponseEntity<RecStopDto> stopRecording(@PathVariable String jobId);
}
