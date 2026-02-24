package svarcondrej.stream_rec.controller;

import org.springframework.http.ResponseEntity;
import svarcondrej.stream_rec.dto.StatusDto;

/**
 * REST controller interface for monitoring the health and status of the Stream Recorder Backend.
 * <p>
 * This interface defines endpoints used by clients to verify that the API is online
 * and responsive.
 * </p>
 */
public interface StatusController {

    /**
     * Retrieves the current operational status of the backend API.
     *
     * @return a {@link ResponseEntity} containing a {@link StatusDto} which includes:
     * <ul>
     * <li><b>status</b>: A string indicating the current state (e.g., "online").</li>
     * <li><b>message</b>: A brief description.</li>
     * <li><b>timestamp</b>: The exact server time the request was processed.</li>
     * </ul>
     */
    public ResponseEntity<StatusDto> getStatus();
}