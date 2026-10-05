package com.forgeci.application.pipeline;

import com.forgeci.domain.pipeline.PipelineDefinition;
import com.forgeci.domain.pipeline.PipelineDefinition.Artifacts;
import com.forgeci.domain.pipeline.PipelineDefinition.Cache;
import com.forgeci.domain.pipeline.PipelineDefinition.Job;
import com.forgeci.domain.pipeline.PipelineDefinition.Pipeline;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class PipelineValidator {
    private static final int SUPPORTED_VERSION = 1;
    private static final int MAX_JOBS = 100;
    private static final int MAX_COMMANDS_PER_JOB = 100;
    private static final int MAX_COMMAND_LENGTH = 8_192;
    private static final int MAX_ENVIRONMENT_VARIABLES = 100;
    private static final int MAX_ENVIRONMENT_VALUE_LENGTH = 8_192;
    private static final int MAX_ARTIFACT_PATHS = 100;
    private static final int MAX_TIMEOUT_SECONDS = 86_400;
    private static final int MAX_RETRIES = 10;
    private static final Pattern ENVIRONMENT_KEY = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    public void validate(PipelineDefinition definition) {
        List<PipelineValidationError> errors = new ArrayList<>();

        if (definition == null) {
            errors.add(error("$", "Configuration is required"));
        } else {
            validateDefinition(definition, errors);
        }

        if (!errors.isEmpty()) {
            throw new PipelineValidationException(errors);
        }
    }

    private void validateDefinition(PipelineDefinition definition, List<PipelineValidationError> errors) {
        if (definition.version() != SUPPORTED_VERSION) {
            errors.add(error("$.version", "Unsupported version; expected 1"));
        }

        Pipeline pipeline = definition.pipeline();
        if (pipeline == null) {
            errors.add(error("$.pipeline", "Pipeline is required"));
            return;
        }

        if (isBlank(pipeline.name())) {
            errors.add(error("$.pipeline.name", "Pipeline name is required"));
        } else if (pipeline.name().length() > 128) {
            errors.add(error("$.pipeline.name", "Pipeline name must be at most 128 characters"));
        }

        validateTriggers(pipeline.triggers(), errors);
        validateDefaults(pipeline.defaults(), errors);

        Map<String, Job> jobs = pipeline.jobs();
        if (jobs == null || jobs.isEmpty()) {
            errors.add(error("$.pipeline.jobs", "At least one job is required"));
            return;
        }
        if (jobs.size() > MAX_JOBS) {
            errors.add(error("$.pipeline.jobs", "A pipeline may contain at most " + MAX_JOBS + " jobs"));
        }

        Set<String> jobNames = new HashSet<>(jobs.keySet());
        for (Map.Entry<String, Job> entry : jobs.entrySet()) {
            String jobName = entry.getKey();
            String base = "$.pipeline.jobs." + jobName;
            if (isBlank(jobName)) {
                errors.add(error("$.pipeline.jobs", "Job names must not be blank"));
                continue;
            }
            if (jobName.length() > 128) {
                errors.add(error(base, "Job name must be at most 128 characters"));
            }
            Job job = entry.getValue();
            if (job == null) {
                errors.add(error(base, "Job definition is required"));
                continue;
            }
            validateJob(job, base, jobNames, errors);
        }

        detectCycles(jobs, errors);
    }

    private void validateJob(Job job, String base, Set<String> jobNames, List<PipelineValidationError> errors) {
        if (isBlank(job.image())) {
            errors.add(error(base + ".image", "Job image is required"));
        } else if (job.image().length() > 512) {
            errors.add(error(base + ".image", "Job image must be at most 512 characters"));
        }

        List<String> commands = job.commands();
        if (commands == null || commands.isEmpty()) {
            errors.add(error(base + ".commands", "At least one command is required"));
        } else {
            if (commands.size() > MAX_COMMANDS_PER_JOB) {
                errors.add(error(base + ".commands", "A job may contain at most " + MAX_COMMANDS_PER_JOB + " commands"));
            }
            for (int i = 0; i < commands.size(); i++) {
                String command = commands.get(i);
                if (isBlank(command)) {
                    errors.add(error(base + ".commands[" + i + "]", "Command must not be blank"));
                } else if (command.length() > MAX_COMMAND_LENGTH) {
                    errors.add(error(base + ".commands[" + i + "]", "Command exceeds 8192 characters"));
                }
            }
        }

        validateDependencies(job.dependsOn(), base, jobNames, errors);
        validateEnvironment(job.environment(), base, errors);
        validateTimeout(job.timeout(), base + ".timeout", errors);
        validateRetries(job.retries(), base + ".retries", errors);
        validateArtifacts(job.artifacts(), base, errors);
        validateCache(job.cache(), base, errors);
        validateWorkingDirectory(job.workingDirectory(), base, errors);
    }

    private void validateDependencies(List<String> dependencies, String base, Set<String> jobNames,
                                      List<PipelineValidationError> errors) {
        if (dependencies == null) {
            return;
        }
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < dependencies.size(); i++) {
            String dependency = dependencies.get(i);
            String path = base + ".depends_on[" + i + "]";
            if (isBlank(dependency)) {
                errors.add(error(path, "Dependency name must not be blank"));
            } else if (!seen.add(dependency)) {
                errors.add(error(path, "Duplicate dependency: " + dependency));
            } else if (!jobNames.contains(dependency)) {
                errors.add(error(path, "Unknown dependency: " + dependency));
            }
        }
    }

    private void validateEnvironment(Map<String, String> environment, String base,
                                     List<PipelineValidationError> errors) {
        if (environment == null) {
            return;
        }
        if (environment.size() > MAX_ENVIRONMENT_VARIABLES) {
            errors.add(error(base + ".environment",
                    "A job may define at most " + MAX_ENVIRONMENT_VARIABLES + " environment variables"));
        }
        for (Map.Entry<String, String> entry : environment.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || !ENVIRONMENT_KEY.matcher(key).matches()) {
                errors.add(error(base + ".environment", "Environment variable names must match [A-Za-z_][A-Za-z0-9_]*"));
            }
            if (value == null) {
                errors.add(error(base + ".environment." + key, "Environment variable value must not be null"));
            } else if (value.length() > MAX_ENVIRONMENT_VALUE_LENGTH) {
                errors.add(error(base + ".environment." + key, "Environment variable value exceeds 8192 characters"));
            }
        }
    }

    private void validateTimeout(Integer timeout, String path, List<PipelineValidationError> errors) {
        if (timeout != null && (timeout < 1 || timeout > MAX_TIMEOUT_SECONDS)) {
            errors.add(error(path, "Timeout must be between 1 and " + MAX_TIMEOUT_SECONDS + " seconds"));
        }
    }

    private void validateRetries(Integer retries, String path, List<PipelineValidationError> errors) {
        if (retries != null && (retries < 0 || retries > MAX_RETRIES)) {
            errors.add(error(path, "Retries must be between 0 and " + MAX_RETRIES));
        }
    }

    private void validateDefaults(PipelineDefinition.Defaults defaults, List<PipelineValidationError> errors) {
        if (defaults == null) {
            return;
        }
        validateTimeout(defaults.timeout(), "$.pipeline.defaults.timeout", errors);
        validateRetries(defaults.retries(), "$.pipeline.defaults.retries", errors);
    }

    private void validateTriggers(List<String> triggers, List<PipelineValidationError> errors) {
        if (triggers == null) {
            return;
        }
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < triggers.size(); i++) {
            String trigger = triggers.get(i);
            if (isBlank(trigger)) {
                errors.add(error("$.pipeline.triggers[" + i + "]", "Trigger must not be blank"));
            } else if (!seen.add(trigger)) {
                errors.add(error("$.pipeline.triggers[" + i + "]", "Duplicate trigger: " + trigger));
            }
        }
    }

    private void validateArtifacts(Artifacts artifacts, String base, List<PipelineValidationError> errors) {
        if (artifacts == null || artifacts.paths() == null) {
            return;
        }
        if (artifacts.paths().size() > MAX_ARTIFACT_PATHS) {
            errors.add(error(base + ".artifacts.paths", "At most " + MAX_ARTIFACT_PATHS + " artifact paths are allowed"));
        }
        for (int i = 0; i < artifacts.paths().size(); i++) {
            String path = artifacts.paths().get(i);
            if (isBlank(path)) {
                errors.add(error(base + ".artifacts.paths[" + i + "]", "Artifact path must not be blank"));
                continue;
            }
            if (isUnsafeRelativePath(path)) {
                errors.add(error(base + ".artifacts.paths[" + i + "]", "Artifact path must be relative and must not traverse parent directories"));
            }
        }
    }

    private void validateCache(Cache cache, String base, List<PipelineValidationError> errors) {
        if (cache == null) {
            return;
        }
        if (isBlank(cache.key())) {
            errors.add(error(base + ".cache.key", "Cache key is required when cache is configured"));
        } else if (cache.key().length() > 256) {
            errors.add(error(base + ".cache.key", "Cache key must be at most 256 characters"));
        }
        if (cache.paths() != null) {
            for (int i = 0; i < cache.paths().size(); i++) {
                String path = cache.paths().get(i);
                if (isBlank(path) || isUnsafeRelativePath(path)) {
                    errors.add(error(base + ".cache.paths[" + i + "]", "Cache path must be relative and must not traverse parent directories"));
                }
            }
        }
    }

    private void validateWorkingDirectory(String workingDirectory, String base, List<PipelineValidationError> errors) {
        if (workingDirectory != null && (isBlank(workingDirectory) || isUnsafeRelativePath(workingDirectory))) {
            errors.add(error(base + ".working_directory",
                    "Working directory must be relative and must not traverse parent directories"));
        }
    }

    private void detectCycles(Map<String, Job> jobs, List<PipelineValidationError> errors) {
        if (jobs == null || jobs.isEmpty()) {
            return;
        }
        Map<String, VisitState> states = new HashMap<>();
        for (String job : jobs.keySet()) {
            states.put(job, VisitState.UNVISITED);
        }
        ArrayDeque<String> path = new ArrayDeque<>();
        for (String job : jobs.keySet()) {
            if (states.get(job) == VisitState.UNVISITED) {
                dfs(job, jobs, states, path, errors);
            }
        }
    }

    private void dfs(String job, Map<String, Job> jobs, Map<String, VisitState> states,
                     ArrayDeque<String> path, List<PipelineValidationError> errors) {
        states.put(job, VisitState.VISITING);
        path.addLast(job);
        Job definition = jobs.get(job);
        if (definition != null && definition.dependsOn() != null) {
            for (String dependency : definition.dependsOn()) {
                if (!jobs.containsKey(dependency)) {
                    continue;
                }
                VisitState state = states.get(dependency);
                if (state == VisitState.VISITING) {
                    errors.add(error("$.pipeline.jobs." + job + ".depends_on",
                            "Dependency cycle detected: " + cyclePath(path, dependency)));
                } else if (state == VisitState.UNVISITED) {
                    dfs(dependency, jobs, states, path, errors);
                }
            }
        }
        path.removeLast();
        states.put(job, VisitState.VISITED);
    }

    private String cyclePath(ArrayDeque<String> path, String repeated) {
        List<String> values = new ArrayList<>(path);
        int start = values.indexOf(repeated);
        if (start < 0) {
            return repeated;
        }
        values = new ArrayList<>(values.subList(start, values.size()));
        values.add(repeated);
        return String.join(" -> ", values);
    }

    private boolean isUnsafeRelativePath(String value) {
        try {
            Path path = Path.of(value.replace('\\', '/'));
            if (path.isAbsolute()) {
                return true;
            }
            for (Path part : path) {
                if ("..".equals(part.toString())) {
                    return true;
                }
            }
            return false;
        } catch (RuntimeException ex) {
            return true;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private PipelineValidationError error(String path, String message) {
        return new PipelineValidationError(path, message);
    }

    private enum VisitState { UNVISITED, VISITING, VISITED }
}