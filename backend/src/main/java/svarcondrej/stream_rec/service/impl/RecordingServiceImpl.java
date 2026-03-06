package svarcondrej.stream_rec.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import svarcondrej.stream_rec.enums.JobStatusEnum;
import svarcondrej.stream_rec.enums.ScheduleRepetitionEnum;
import svarcondrej.stream_rec.enums.ScheduleStatusEnum;
import svarcondrej.stream_rec.model.RecordingFile;
import svarcondrej.stream_rec.model.RecordingJob;
import svarcondrej.stream_rec.model.RecordingSchedule;
import svarcondrej.stream_rec.repository.RecordingFileRepository;
import svarcondrej.stream_rec.repository.RecordingJobRepository;
import svarcondrej.stream_rec.repository.RecordingScheduleRepository;
import svarcondrej.stream_rec.service.ScheduleManagerService;
import svarcondrej.stream_rec.util.DatabaseLock;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RecordingServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(RecordingServiceImpl.class);

    private final Map<String, Process> activeProcesses = new ConcurrentHashMap<>();
    private final Map<String, String> activeJobIds = new ConcurrentHashMap<>();

    private final RecordingScheduleRepository scheduleRepository;
    private final RecordingJobRepository jobRepository;
    private final ScheduleManagerService scheduleManagerService;
    private final RecordingFileRepository recordingFileRepository;

    public RecordingServiceImpl (RecordingScheduleRepository scheduleRepository,
                                RecordingJobRepository jobRepository,
                                @Lazy ScheduleManagerService scheduleManagerService,
                                 RecordingFileRepository recordingFileRepository) {
        this.scheduleRepository = scheduleRepository;
        this.jobRepository = jobRepository;
        this.scheduleManagerService = scheduleManagerService;
        this.recordingFileRepository = recordingFileRepository;
    }

    @Async
    public void startRecording (String scheduleId) {
        RecordingSchedule schedule = scheduleRepository.findById(scheduleId).orElse(null);
        if ( schedule == null || schedule.getStatus() == ScheduleStatusEnum.PAUSED ) {
            logger.warn("Schedule {} not found or paused. Aborting start.", scheduleId);
            return;
        }

        logger.info("Starting recording for Schedule: {}", schedule.getName());

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        String safeName = schedule.getName().replaceAll("[^a-zA-Z0-9.-]", "_");
        String filename = safeName + "_" + timestamp + ".mp4";
        String fullPath = "/app/recordings/" + filename;

        RecordingJob job = new RecordingJob();
        job.setSchedule(schedule);
        job.setActualStartTime(LocalDateTime.now());
        job.setFilePath(fullPath);
        job.setStatus(JobStatusEnum.RECORDING);

        DatabaseLock.WRITE_LOCK.lock();
        try {
            job = jobRepository.save(job);
        } finally {
            DatabaseLock.WRITE_LOCK.unlock();
        }

        activeJobIds.put(scheduleId, job.getId());

        ProcessBuilder processBuilder = new ProcessBuilder(
                "yt-dlp",
                "-o", fullPath,
                "--merge-output-format", "mp4",
                schedule.getStreamUrl()
        );
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            activeProcesses.put(scheduleId, process);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ( (line = reader.readLine()) != null ) {
                    logger.debug("[Job {}] {}", job.getId(), line);
                }
            } catch (IOException e) {
                if ( e.getMessage() == null || !e.getMessage().contains("Stream closed") ) {
                    logger.error("Error reading stream for job: {}", job.getId(), e);
                }
            }

            int exitCode = process.waitFor();
            logger.info("Recording job {} finished with exit code {}", job.getId(), exitCode);

        } catch (Exception e) {
            logger.error("Error during recording job: {}", job.getId(), e);
            updateJobStatus(job.getId(), JobStatusEnum.FAILED);
        } finally {
            activeProcesses.remove(scheduleId);
            String finishedJobId = activeJobIds.remove(scheduleId);

            if (finishedJobId != null) {
                updateJobStatus(finishedJobId, JobStatusEnum.COMPLETED);
            }

            handleRepetition(schedule);
        }
    }

    public void stopRecording (String scheduleId) {
        Process process = activeProcesses.get(scheduleId);
        String jobId = activeJobIds.get(scheduleId);

        if ( process != null && process.isAlive() ) {
            logger.info("Sending graceful stop signal to Schedule: {}", scheduleId);
            try {
                long pid = process.pid();
                Runtime.getRuntime().exec("kill -SIGINT " + pid);

                boolean exitedCleanly = process.waitFor(15, TimeUnit.SECONDS);

                if ( !exitedCleanly ) {
                    logger.warn("Schedule {} did not shut down gracefully. Forcing kill...", scheduleId);
                    process.destroyForcibly();
                }
            } catch (Exception e) {
                logger.error("Error while attempting to stop schedule: {}", scheduleId, e);
                process.destroy();
                if ( jobId != null ) updateJobStatus(jobId, JobStatusEnum.FAILED);
            }
        } else {
            logger.warn("No active recording found for Schedule: {}", scheduleId);
        }
    }

    private void updateJobStatus (String jobId, JobStatusEnum newStatus) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus(newStatus);
            if ( newStatus == JobStatusEnum.COMPLETED || newStatus == JobStatusEnum.FAILED ) {
                job.setActualEndTime(LocalDateTime.now());

                File file = new File(job.getFilePath());
                if ( !file.exists() || file.length() == 0 ) {
                    job.setStatus(JobStatusEnum.FAILED);
                    logger.error("Job {} marked as FAILED because output file is missing or empty.", jobId);
                } else if ( newStatus == JobStatusEnum.COMPLETED ) {
                    RecordingFile recordingFile = new RecordingFile();
                    recordingFile.setFilename(file.getName());
                    recordingFile.setFilepath(job.getFilePath());
                    recordingFile.setOwner(job.getSchedule().getUser());

                    DatabaseLock.WRITE_LOCK.lock();
                    try {
                        recordingFileRepository.save(recordingFile);
                        jobRepository.save(job);
                    } finally {
                        DatabaseLock.WRITE_LOCK.unlock();
                    }

                    logger.info("Registered new RecordingFile: {}", recordingFile.getFilename());
                    return;
                }
            }
            DatabaseLock.WRITE_LOCK.lock();
            try {
                jobRepository.save(job);
            } finally {
                DatabaseLock.WRITE_LOCK.unlock();
            }
        });
    }

    private void handleRepetition (RecordingSchedule schedule) {
        if ( schedule.getRepetition() == ScheduleRepetitionEnum.NONE ) {
            schedule.setStatus(ScheduleStatusEnum.DONE);
            scheduleRepository.save(schedule);
            logger.info("Schedule {} marked as DONE.", schedule.getId());
            return;
        }

        LocalDateTime nextRun = schedule.getStartTime();

        switch ( schedule.getRepetition() ) {
            case DAILY -> nextRun = nextRun.plusDays(1);
            case WEEKLY -> nextRun = nextRun.plusWeeks(1);
            case MONTHLY -> nextRun = nextRun.plusMonths(1);
            case YEARLY -> nextRun = nextRun.plusYears(1);
            case CUSTOM -> {
                if ( schedule.getCustomRepetitionDays() != null ) {
                    nextRun = nextRun.plusDays(schedule.getCustomRepetitionDays());
                } else {
                    nextRun = nextRun.plusDays(1); // Fallback
                }
            }
        }

        while ( nextRun.isBefore(LocalDateTime.now()) ) {
            switch ( schedule.getRepetition() ) {
                case DAILY -> nextRun = nextRun.plusDays(1);
                case WEEKLY -> nextRun = nextRun.plusWeeks(1);
                case MONTHLY -> nextRun = nextRun.plusMonths(1);
                case YEARLY -> nextRun = nextRun.plusYears(1);
                case CUSTOM -> nextRun = nextRun.plusDays(schedule.getCustomRepetitionDays() != null ? schedule.getCustomRepetitionDays() : 1);
            }
        }

        schedule.setStartTime(nextRun);

        DatabaseLock.WRITE_LOCK.lock();
        try {
            scheduleRepository.save(schedule);
        } finally {
            DatabaseLock.WRITE_LOCK.unlock();
        }

        logger.info("Schedule {} is repeating. Next run time updated to: {}", schedule.getId(), nextRun);

        scheduleManagerService.queueQuartzJobs(schedule);
    }
}