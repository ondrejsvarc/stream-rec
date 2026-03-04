package svarcondrej.stream_rec.service;

import svarcondrej.stream_rec.exception.RecordingJobNotFoundException;

/**
 * Service interface for managing the lifecycle of stream recordings.
 */
public interface RecordingService {

    /**
     * Asynchronously starts a recording process for the specified stream URL.
     * <p>
     * The recording runs in the background, capturing the stream output and writing
     * it to the local file system. The provided job ID is used to track the process
     * in memory and to name the resulting media file.
     * </p>
     *
     * @param streamUrl the target URL of the media stream to record
     * @param jobId     a unique identifier assigned to this specific recording job
     */
    public void startRecording(String streamUrl, String jobId);

    /**
     * Attempts to stop an actively running recording job.
     * <p>
     * This method sends a graceful shutdown signal to the underlying recording
     * process to allow it to safely multiplex and finalize the
     * video file. If the process fails to terminate gracefully within a predefined
     * timeout period, it will be forcefully killed.
     * </p>
     * <p>
     * If no active recording matches the provided job ID, the method will throw
     * a {@link RecordingJobNotFoundException}.
     * </p>
     *
     * @param jobId the unique identifier of the active recording job to terminate
     */
    public void stopRecording(String jobId);
}
