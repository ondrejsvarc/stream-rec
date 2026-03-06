package svarcondrej.stream_rec.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import svarcondrej.stream_rec.service.impl.RecordingServiceImpl;

@Component
public class StreamRecordingJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(StreamRecordingJob.class);
    private final RecordingServiceImpl recordingService;

    public StreamRecordingJob (RecordingServiceImpl recordingService) {
        this.recordingService = recordingService;
    }

    @Override
    public void execute (JobExecutionContext context) {
        JobDataMap dataMap = context.getMergedJobDataMap();

        String action = dataMap.getString("action");
        String scheduleId = dataMap.getString("scheduleId");

        logger.info("Quartz triggered action: {} for Schedule ID: {}", action, scheduleId);

        if ( "START".equals(action) ) {
            recordingService.startRecording(scheduleId);
        } else if ( "STOP".equals(action) ) {
            recordingService.stopRecording(scheduleId);
        }
    }
}
