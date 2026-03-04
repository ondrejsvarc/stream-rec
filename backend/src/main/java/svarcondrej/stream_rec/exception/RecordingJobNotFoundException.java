package svarcondrej.stream_rec.exception;

public class RecordingJobNotFoundException extends RuntimeException {
    public RecordingJobNotFoundException(String message) {
        super(message);
    }
}
