package svarcondrej.stream_rec.service.impl;

import svarcondrej.stream_rec.enums.JobActionEnum;
import svarcondrej.stream_rec.enums.JobStatusEnum;
import svarcondrej.stream_rec.job.StreamRecordingJob;
import svarcondrej.stream_rec.model.RecordingSchedule;
import svarcondrej.stream_rec.repository.RecordingScheduleRepository;
import jakarta.annotation.PostConstruct;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import svarcondrej.stream_rec.service.ScheduleManagerService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class ScheduleManagerServiceImpl implements ScheduleManagerService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleManagerServiceImpl.class);

    private final Scheduler scheduler;
    private final RecordingScheduleRepository repository;

    public ScheduleManagerServiceImpl(Scheduler scheduler, RecordingScheduleRepository repository) {
        this.scheduler = scheduler;
        this.repository = repository;
    }

    public RecordingSchedule createSchedule(String streamUrl, LocalDateTime startTime, LocalDateTime endTime) {
        RecordingSchedule schedule = new RecordingSchedule();
        schedule.setStreamUrl(streamUrl);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setStatus(JobStatusEnum.SCHEDULED);

        schedule = repository.save(schedule);
        logger.info("Saved new schedule to database with ID: {}", schedule.getId());

        scheduleQuartzJob(schedule, JobActionEnum.START, startTime);
        scheduleQuartzJob(schedule, JobActionEnum.STOP, endTime);

        return schedule;
    }

    private void scheduleQuartzJob(RecordingSchedule schedule, JobActionEnum action, LocalDateTime triggerTime) {
        try {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("action", action.name());
            jobDataMap.put("streamUrl", schedule.getStreamUrl());
            jobDataMap.put("jobId", schedule.getId());

            JobDetail jobDetail = JobBuilder.newJob(StreamRecordingJob.class)
                    .withIdentity(schedule.getId() + "_" + action, "recording-jobs")
                    .usingJobData(jobDataMap)
                    .build();

            Date runTime = Date.from(triggerTime.atZone(ZoneId.systemDefault()).toInstant());

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(schedule.getId() + "_" + action + "_trigger", "recording-triggers")
                    .startAt(runTime)
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            logger.info("Scheduled {} action for Job {} at {}", action, schedule.getId(), runTime);

        } catch (SchedulerException e) {
            logger.error("Failed to schedule {} job for ID: {}", action, schedule.getId(), e);
        }
    }

    @PostConstruct
    public void syncSchedulesOnStartup() {
        logger.info("Syncing pending schedules from SQLite to Quartz...");
        List<RecordingSchedule> pendingSchedules = repository.findByStatus(JobStatusEnum.SCHEDULED);

        pendingSchedules.addAll(repository.findByStatus(JobStatusEnum.RECORDING));

        LocalDateTime now = LocalDateTime.now();

        for ( RecordingSchedule schedule : pendingSchedules ) {
            if ( schedule.getEndTime().isAfter(now) ) {
                if ( schedule.getStartTime().isAfter(now) ) {
                    scheduleQuartzJob(schedule, JobActionEnum.START, schedule.getStartTime());
                } else if (schedule.getStatus().equals(JobStatusEnum.RECORDING)) {
                    scheduleQuartzJob(schedule, JobActionEnum.START, now.plusSeconds(10));
                }
                scheduleQuartzJob(schedule, JobActionEnum.STOP, schedule.getEndTime());
            } else {
                schedule.setStatus(JobStatusEnum.EXPIRED);
                repository.save(schedule);
            }
        }
    }
}
