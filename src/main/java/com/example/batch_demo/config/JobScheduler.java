package com.example.batch_demo.config;

import com.example.batch_demo.entity.JobConfig;
import com.example.batch_demo.repository.JobConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class JobScheduler implements SchedulingConfigurer {

    private final JobLauncher jobLauncher;
    private final Job posSaleJob;
    private final JobConfigRepository jobConfigRepository;
    private static final ZoneId TOKYO_ZONE = ZoneId.of("Asia/Tokyo");

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        log.info("Configuring job scheduler with timezone: {}", TOKYO_ZONE);
        
        taskRegistrar.addTriggerTask(
            () -> {
                try {
                    Optional<JobConfig> jobConfigOpt = jobConfigRepository.findByJobNameAndEnabled("posSaleJob", true);
                    if (jobConfigOpt.isPresent()) {
                        JobConfig jobConfig = jobConfigOpt.get();
                        ZonedDateTime nowTokyo = ZonedDateTime.now(TOKYO_ZONE);
                        LocalDateTime now = nowTokyo.toLocalDateTime();
                        
                        log.info("Checking job execution. Current time (JST): {}, Last run: {}, Next run: {}", 
                                now, jobConfig.getLastRun(), jobConfig.getNextRun());
                        
                        if (jobConfig.getLastRun() == null || 
                            (jobConfig.getNextRun() != null && now.isAfter(jobConfig.getNextRun()))) {
                            log.info("Starting job execution for: {}", jobConfig.getJobName());
                            runJob();
                            
                            // Update last run and next run time
                            jobConfig.setLastRun(now);
                            CronTrigger trigger = new CronTrigger(jobConfig.getCronExpression());
                            Date nextExecutionTime = trigger.nextExecutionTime(null);
                            jobConfig.setNextRun(LocalDateTime.ofInstant(nextExecutionTime.toInstant(), TOKYO_ZONE));
                            
                            jobConfigRepository.save(jobConfig);
                            log.info("Job execution completed. Updated next run time to: {}", jobConfig.getNextRun());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error executing job", e);
                }
            },
            triggerContext -> {
                Optional<JobConfig> jobConfig = jobConfigRepository.findByJobNameAndEnabled("posSaleJob", true);
                String cronExpression = jobConfig
                    .map(JobConfig::getCronExpression)
                    .orElse("0 20 5 * * ?"); // Default to 05:20 JST
                CronTrigger trigger = new CronTrigger(cronExpression);
                Date nextExecutionTime = trigger.nextExecutionTime(triggerContext);
                log.debug("Next execution time calculated: {}", nextExecutionTime);
                return nextExecutionTime.toInstant();
            }
        );
    }

    private void runJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(posSaleJob, jobParameters);
    }
} 