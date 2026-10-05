package com.forgeci.application.pipeline;

import com.forgeci.domain.pipeline.PipelineDag;
import com.forgeci.domain.pipeline.PipelineDefinition.Job;
import com.forgeci.domain.pipeline.PipelineDefinition.Pipeline;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PipelineDagBuilder {
    public PipelineDag build(Pipeline pipeline) {
        if (pipeline == null || pipeline.jobs() == null) {
            throw new PipelineValidationException(
                    List.of(new PipelineValidationError("$.pipeline.jobs", "Jobs are required to build the DAG")));
        }

        Map<String, Set<String>> dependencies = new LinkedHashMap<>();
        Map<String, Set<String>> dependents = new LinkedHashMap<>();
        Map<String, Integer> indegree = new HashMap<>();

        pipeline.jobs().keySet().stream().sorted().forEach(name -> {
            dependencies.put(name, new LinkedHashSet<>());
            dependents.put(name, new LinkedHashSet<>());
            indegree.put(name, 0);
        });

        for (Map.Entry<String, Job> entry : pipeline.jobs().entrySet()) {
            String jobName = entry.getKey();
            List<String> declaredDependencies = entry.getValue() == null ? null : entry.getValue().dependsOn();
            if (declaredDependencies == null) {
                continue;
            }
            for (String dependency : declaredDependencies) {
                if (!dependencies.containsKey(dependency)) {
                    continue;
                }
                dependencies.get(jobName).add(dependency);
                dependents.get(dependency).add(jobName);
                indegree.put(jobName, indegree.get(jobName) + 1);
            }
        }

        ArrayDeque<String> ready = new ArrayDeque<>();
        indegree.entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .sorted()
                .forEach(ready::addLast);

        List<String> order = new ArrayList<>();
        while (!ready.isEmpty()) {
            String job = ready.removeFirst();
            order.add(job);

            List<String> nextJobs = new ArrayList<>(dependents.getOrDefault(job, Collections.emptySet()));
            nextJobs.sort(Comparator.naturalOrder());
            for (String next : nextJobs) {
                int nextIndegree = indegree.merge(next, -1, Integer::sum);
                if (nextIndegree == 0) {
                    ready.addLast(next);
                }
            }
        }

        if (order.size() != pipeline.jobs().size()) {
            throw new PipelineValidationException(
                    List.of(new PipelineValidationError("$.pipeline.jobs", "Dependency cycle detected")));
        }

        return new PipelineDag(order, dependencies, dependents);
    }
}