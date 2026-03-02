package svarcondrej.stream_rec.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import svarcondrej.stream_rec.service.RecordingService;

@Component
public class StreamRecordingJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(StreamRecordingJob.class);
    private final RecordingService recordingService;

    public StreamRecordingJob(RecordingService recordingService) {
        this.recordingService = recordingService;
    }

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getMergedJobDataMap();

        String action = dataMap.getString("action");
        String streamUrl = dataMap.getString("streamUrl");
        String jobId = dataMap.getString("jobId");

        logger.info("Quartz triggered action: {} for Job ID: {}", action, jobId);

        if ("START".equals(action)) {
            recordingService.startRecording(streamUrl, jobId);
        } else if ("STOP".equals(action)) {
            recordingService.stopRecording(jobId);
        }
    }
}
