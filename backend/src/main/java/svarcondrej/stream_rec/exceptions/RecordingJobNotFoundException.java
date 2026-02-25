package svarcondrej.stream_rec.exceptions;

public class RecordingJobNotFoundException extends RuntimeException {
    public RecordingJobNotFoundException(String message) {
        super(message);
    }
}
