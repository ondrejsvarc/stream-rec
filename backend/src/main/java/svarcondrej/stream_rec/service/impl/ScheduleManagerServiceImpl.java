package svarcondrej.stream_rec.service.impl;

import jakarta.annotation.PostConstruct;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import svarcondrej.stream_rec.dto.ScheduleRequestDto;
import svarcondrej.stream_rec.enums.CodecEnum;
import svarcondrej.stream_rec.enums.RoleEnum;
import svarcondrej.stream_rec.enums.ScheduleRepetitionEnum;
import svarcondrej.stream_rec.enums.ScheduleStatusEnum;
import svarcondrej.stream_rec.exception.InsufficientPermissionsException;
import svarcondrej.stream_rec.exception.ScheduleNotFoundException;
import svarcondrej.stream_rec.exception.UserNotFoundException;
import svarcondrej.stream_rec.job.StreamRecordingJob;
import svarcondrej.stream_rec.model.RecordingSchedule;
import svarcondrej.stream_rec.model.User;
import svarcondrej.stream_rec.repository.RecordingScheduleRepository;
import svarcondrej.stream_rec.repository.UserRepository;
import svarcondrej.stream_rec.service.ScheduleManagerService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class ScheduleManagerServiceImpl implements ScheduleManagerService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleManagerService.class);

    private final Scheduler scheduler;
    private final RecordingScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    public ScheduleManagerServiceImpl (Scheduler scheduler, RecordingScheduleRepository scheduleRepository, UserRepository userRepository) {
        this.scheduler = scheduler;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
    }

    public RecordingSchedule createSchedule (ScheduleRequestDto request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        RecordingSchedule schedule = mapRequestToEntity(request, new RecordingSchedule());
        schedule.setUser(user);
        schedule.setStatus(ScheduleStatusEnum.SCHEDULED);

        schedule = scheduleRepository.save(schedule);
        logger.info("Created new schedule ID: {} for user: {}", schedule.getId(), username);

        queueQuartzJobs(schedule);
        return schedule;
    }

    public RecordingSchedule updateSchedule (String scheduleId, ScheduleRequestDto request, String username) {
        RecordingSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found: " + scheduleId));

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        boolean isAdmin = currentUser.getRole() == RoleEnum.ADMIN;
        if ( !isAdmin && !schedule.getUser().getId().equals(currentUser.getId()) ) {
            throw new InsufficientPermissionsException("You do not have permission to edit this schedule.");
        }

        removeQuartzJobs(schedule.getId());

        schedule = mapRequestToEntity(request, schedule);
        schedule = scheduleRepository.save(schedule);

        logger.info("Updated schedule ID: {} by user: {}", schedule.getId(), username);

        if ( schedule.getStatus() != ScheduleStatusEnum.PAUSED ) {
            queueQuartzJobs(schedule);
        }

        return schedule;
    }

    public void deleteSchedule (String scheduleId, String username) {
        RecordingSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found: " + scheduleId));

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        if ( currentUser.getRole() != RoleEnum.ADMIN && !schedule.getUser().getId().equals(currentUser.getId()) ) {
            throw new InsufficientPermissionsException("You do not have permission to delete this schedule.");
        }

        removeQuartzJobs(scheduleId);
        scheduleRepository.delete(schedule);
        logger.info("Deleted schedule ID: {}", scheduleId);
    }

    private RecordingSchedule mapRequestToEntity (ScheduleRequestDto request, RecordingSchedule schedule) {
        schedule.setName(request.getName());
        schedule.setStreamUrl(request.getStreamUrl());
        schedule.setStartTime(request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now().plusSeconds(5));
        schedule.setDuration(request.getDuration());

        try {
            schedule.setRepetition(ScheduleRepetitionEnum.valueOf(request.getRepetition().toUpperCase()));
        } catch (Exception e) {
            schedule.setRepetition(ScheduleRepetitionEnum.NONE);
        }

        schedule.setCustomRepetitionDays(request.getCustomRepetitionDays());

        try {
            schedule.setCodec(request.getCodec() != null ? CodecEnum.valueOf(request.getCodec().toUpperCase()) : CodecEnum.NONE);
        } catch (Exception e) {
            schedule.setCodec(CodecEnum.NONE);
        }

        schedule.setMaxTranscodeBitrate(request.getMaxTranscodeBitrate());
        return schedule;
    }

    public void queueQuartzJobs (RecordingSchedule schedule) {
        LocalDateTime start = schedule.getStartTime();

        if ( start.isBefore(LocalDateTime.now()) && schedule.getRepetition() == ScheduleRepetitionEnum.NONE ) {
            start = LocalDateTime.now();
        }

        scheduleQuartzJob(schedule, "START", start);

        if ( schedule.getDuration() != null && schedule.getDuration() > 0 ) {
            LocalDateTime stop = start.plusMinutes(schedule.getDuration());
            scheduleQuartzJob(schedule, "STOP", stop);
        }
    }

    private void scheduleQuartzJob (RecordingSchedule schedule, String action, LocalDateTime triggerTime) {
        try {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("action", action);
            jobDataMap.put("scheduleId", schedule.getId());
            jobDataMap.put("streamUrl", schedule.getStreamUrl());

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
            logger.info("Scheduled {} action for Schedule {} at {}", action, schedule.getId(), runTime);

        } catch (SchedulerException e) {
            logger.error("Failed to schedule {} job for ID: {}", action, schedule.getId(), e);
        }
    }

    private void removeQuartzJobs(String scheduleId) {
        try {
            scheduler.deleteJob(new JobKey(scheduleId + "_START", "recording-jobs"));
            scheduler.deleteJob(new JobKey(scheduleId + "_STOP", "recording-jobs"));
        } catch (SchedulerException e) {
            logger.error("Failed to remove existing Quartz jobs for Schedule ID: {}", scheduleId, e);
        }
    }

    @PostConstruct
    public void syncSchedulesOnStartup() {
        logger.info("Syncing pending schedules from SQLite to Quartz...");
        List<RecordingSchedule> pendingSchedules = scheduleRepository.findByStatus(ScheduleStatusEnum.SCHEDULED);

        for ( RecordingSchedule schedule : pendingSchedules ) {
            queueQuartzJobs(schedule);
        }
    }
}
