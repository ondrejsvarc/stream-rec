package svarcondrej.stream_rec.service;

import svarcondrej.stream_rec.exception.RecordingJobNotFoundException;


public interface RecordingService {

    public void startRecording (String scheduleId);

    public void stopRecording (String scheduleId);
}
