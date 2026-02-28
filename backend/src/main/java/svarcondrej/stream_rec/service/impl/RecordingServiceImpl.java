package svarcondrej.stream_rec.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import svarcondrej.stream_rec.enums.JobStatusEnum;
import svarcondrej.stream_rec.exceptions.RecordingJobNotFoundException;
import svarcondrej.stream_rec.exceptions.StreamReadException;
import svarcondrej.stream_rec.repository.RecordingScheduleRepository;
import svarcondrej.stream_rec.service.RecordingService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RecordingServiceImpl implements RecordingService {

    private static final Logger logger = LoggerFactory.getLogger(RecordingServiceImpl.class);
    private final Map<String, Process> activeRecordings = new ConcurrentHashMap<>();

    private final RecordingScheduleRepository repository;

    public RecordingServiceImpl(RecordingScheduleRepository repository) {
        this.repository = repository;
    }

    @Async
    public void startRecording(String streamUrl, String jobId) {
        logger.info("Starting recording for job: {} - URL: {}", jobId, streamUrl);

        ProcessBuilder processBuilder = new ProcessBuilder(
                "yt-dlp",
                "-o", "/app/recordings/" + jobId + ".%(ext)s",
                streamUrl
        );

        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            activeRecordings.put(jobId, process);

            updateJobStatus(jobId, JobStatusEnum.RECORDING);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.debug("[Job {}] {}", jobId, line);
                }
            } catch (IOException e) {
                if (e.getMessage() != null && e.getMessage().contains("Stream closed")) {
                    logger.info("Log stream closed normally during shutdown for job: {}", jobId);
                } else {
                    logger.error("Error reading stream for job: {}", jobId, e);
                    throw new StreamReadException(e.getMessage());
                }
            }

            int exitCode = process.waitFor();
            logger.info("Recording job {} finished with exit code {}", jobId, exitCode);

        } catch (Exception e) {
            logger.error("Error during recording job: {}", jobId, e);
        } finally {
            activeRecordings.remove(jobId);
        }
    }

    public void stopRecording(String jobId) {
        Process process = activeRecordings.get(jobId);

        if (process != null && process.isAlive()) {
            logger.info("Sending graceful stop signal (SIGINT) to job: {}", jobId);
            try {
                long pid = process.pid();

                Runtime.getRuntime().exec("kill -SIGINT " + pid);

                boolean exitedCleanly = process.waitFor(15, TimeUnit.SECONDS);

                if (!exitedCleanly) {
                    logger.warn("Job {} did not shut down gracefully in time. Forcing kill...", jobId);
                    process.destroyForcibly();
                } else {
                    logger.info("Job {} stopped and finalized the video successfully.", jobId);
                }

                updateJobStatus(jobId, JobStatusEnum.COMPLETED);

            } catch (Exception e) {
                logger.error("Error while attempting to stop job: {}", jobId, e);
                process.destroy();
            }
        } else {
            logger.warn("No active recording found for job: {}", jobId);
            throw new RecordingJobNotFoundException("No active recording found for job: " + jobId);
        }
    }

    private void updateJobStatus(String jobId, JobStatusEnum newStatus) {
        repository.findById(jobId).ifPresent(schedule -> {
            schedule.setStatus(newStatus);
            repository.save(schedule);
            logger.info("Updated Job {} status to {}", jobId, newStatus);
        });
    }
}