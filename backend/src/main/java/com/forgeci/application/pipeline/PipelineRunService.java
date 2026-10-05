package com.forgeci.application.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgeci.domain.pipeline.JobRun;
import com.forgeci.domain.pipeline.PipelineDefinition.Job;
import com.forgeci.domain.pipeline.PipelineRun;
import com.forgeci.domain.pipeline.PipelineRunStatus;
import com.forgeci.application.pipeline.PipelineConfiguration;
import com.forgeci.domain.pipeline.StepRun;
import com.forgeci.infrastructure.pipeline.JobRunRepository;
import com.forgeci.infrastructure.pipeline.PipelineRunRepository;
import com.forgeci.infrastructure.pipeline.StepRunRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PipelineRunService {
    private static final String FORGECI_VERSION = "0.1.0";

    private final PipelineRunRepository pipelineRuns;
    private final JobRunRepository jobRuns;
    private final StepRunRepository stepRuns;
    private final ObjectMapper objectMapper;

    public PipelineRunService(PipelineRunRepository pipelineRuns, JobRunRepository jobRuns,
                              StepRunRepository stepRuns, ObjectMapper objectMapper) {
        this.pipelineRuns = pipelineRuns;
        this.jobRuns = jobRuns;
        this.stepRuns = stepRuns;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PipelineRun create(UUID repositoryId, String commitSha, String branch, String trigger,
                              String pipelineYaml, PipelineConfiguration configuration) {
        PipelineRun run = new PipelineRun(
                repositoryId,
                commitSha,
                branch,
                trigger,
                pipelineYaml,
                serialize(configuration.definition()),
                serialize(configuration.dag()),
                FORGECI_VERSION
        );
        run = pipelineRuns.save(run);

        List<JobRun> jobs = new ArrayList<>();
        for (String jobName : configuration.dag().topologicalOrder()) {
            Job job = configuration.definition().pipeline().jobs().get(jobName);
            JobRun jobRun = jobRuns.save(new JobRun(
                    run.getId(),
                    jobName,
                    job.image(),
                    serialize(job.commands()),
                    serialize(job.dependsOn() == null ? List.of() : job.dependsOn())
            ));
            jobs.add(jobRun);
        }

        for (JobRun jobRun : jobs) {
            List<String> commands = readStringList(jobRun.getCommands());
            for (int i = 0; i < commands.size(); i++) {
                stepRuns.save(new StepRun(jobRun.getId(), i, commands.get(i)));
            }
        }
        return run;
    }

    @Transactional(readOnly = true)
    public List<PipelineRun> list(UUID repositoryId) {
        return pipelineRuns.findAllByRepositoryIdOrderByCreatedAtDesc(repositoryId);
    }

    @Transactional
    public PipelineRun queue(UUID id) { return mutate(id, PipelineRun::queue); }

    @Transactional
    public PipelineRun start(UUID id) { return mutate(id, PipelineRun::start); }

    @Transactional
    public PipelineRun succeed(UUID id) { return mutate(id, PipelineRun::succeed); }

    @Transactional
    public PipelineRun fail(UUID id) { return mutate(id, PipelineRun::fail); }

    @Transactional
    public PipelineRun cancel(UUID id) { return mutate(id, PipelineRun::cancel); }

    @Transactional
    public PipelineRun timeOut(UUID id) { return mutate(id, PipelineRun::timeOut); }

    private PipelineRun mutate(UUID id, java.util.function.Consumer<PipelineRun> action) {
        PipelineRun run = pipelineRuns.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pipeline run not found"));
        action.accept(run);
        return pipelineRuns.save(run);
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to snapshot pipeline configuration", e);
        }
    }

    private List<String> readStringList(String value) {
        try {
            return objectMapper.readValue(value, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid persisted command snapshot", e);
        }
    }
}