package com.forgeci.application.pipeline;

import com.forgeci.config.ForgeCiRabbitConfig;
import com.forgeci.domain.pipeline.JobRun;
import com.forgeci.domain.pipeline.JobRunStatus;
import com.forgeci.domain.pipeline.PipelineRun;
import com.forgeci.domain.repository.RepositoryConnection;
import com.forgeci.infrastructure.pipeline.JobRunRepository;
import com.forgeci.infrastructure.pipeline.PipelineRunRepository;
import com.forgeci.infrastructure.pipeline.ProcessedMessageRepository;
import com.forgeci.infrastructure.repository.RepositoryConnectionRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Publishes Phase 9 JobMessage to forgeci.jobs (no secrets in the payload). */
@Service
public class JobPublisher {
    private static final String PUBLISHER = "job-publisher";
    private static final Logger log = LoggerFactory.getLogger(JobPublisher.class);

    private final JobRunRepository jobs;
    private final PipelineRunRepository runs;
    private final RepositoryConnectionRepository repositories;
    private final ProcessedMessageRepository processedMessages;
    private final RabbitTemplate rabbitTemplate;

    public JobPublisher(
            JobRunRepository jobs,
            PipelineRunRepository runs,
            RepositoryConnectionRepository repositories,
            ProcessedMessageRepository processedMessages,
            RabbitTemplate rabbitTemplate) {
        this.jobs = jobs;
        this.runs = runs;
        this.repositories = repositories;
        this.processedMessages = processedMessages;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelayString = "${forgeci.pipeline.publisher-delay-ms:500}")
    @Transactional
    public void publishQueuedJobs() {
        for (JobRun job : jobs.findAllByStatus(JobRunStatus.QUEUED)) {
            UUID publishKey = job.getId();
            if (processedMessages.existsByMessageIdAndConsumer(publishKey, PUBLISHER)) {
                continue;
            }
            try {
                publish(job);
                processedMessages.claim(UUID.randomUUID(), publishKey, PUBLISHER);
            } catch (RuntimeException e) {
                log.warn("Failed to publish job {}: {}", job.getId(), e.getMessage());
            }
        }
    }

    private void publish(JobRun job) {
        PipelineRun run = runs.findById(job.getPipelineRunId()).orElse(null);
        if (run == null) {
            return;
        }
        RepositoryConnection repo = repositories.findById(run.getRepositoryId()).orElse(null);
        UUID organizationId = repo != null && repo.getOrganization() != null
                ? repo.getOrganization().getId()
                : run.getRepositoryId();

        JobMessage message = JobMessage.create(
                job.getId(),
                job.getAttemptId(),
                run.getRepositoryId(),
                run.getId(),
                organizationId,
                UUID.randomUUID());

        rabbitTemplate.convertAndSend(
                ForgeCiRabbitConfig.EXCHANGE,
                ForgeCiRabbitConfig.JOBS_ROUTING_KEY,
                message);
    }
}
