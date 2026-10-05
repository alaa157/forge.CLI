package com.forgeci.domain.pipeline;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record PipelineDag(
        List<String> topologicalOrder,
        Map<String, Set<String>> dependencies,
        Map<String, Set<String>> dependents
) {
    public PipelineDag {
        topologicalOrder = List.copyOf(topologicalOrder);
        dependencies = Map.copyOf(dependencies);
        dependents = Map.copyOf(dependents);
    }
}