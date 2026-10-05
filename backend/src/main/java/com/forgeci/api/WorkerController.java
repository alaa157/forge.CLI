package com.forgeci.api;

import com.forgeci.application.worker.WorkerService;
import com.forgeci.domain.worker.Worker;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/workers")
public class WorkerController {
    private final WorkerService workers;

    public WorkerController(WorkerService workers) {
        this.workers = workers;
    }

    public record RegisterRequest(
            UUID workerId,
            @NotBlank String hostname,
            @NotBlank String version,
            String capabilities,
            @Min(1) @Max(256) int maxConcurrency) {}

    public record WorkerResponse(
            UUID workerId,
            String hostname,
            String version,
            String status,
            int maxConcurrency) {
        static WorkerResponse from(Worker w) {
            return new WorkerResponse(
                    w.getId(), w.getHostname(), w.getVersion(), w.getStatus().name(), w.getMaxConcurrency());
        }
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkerResponse register(@RequestBody RegisterRequest body) {
        int concurrency = body.maxConcurrency() <= 0 ? 1 : body.maxConcurrency();
        Worker w = workers.register(
                body.workerId(),
                body.hostname(),
                body.version(),
                body.capabilities() == null ? "[]" : body.capabilities(),
                concurrency);
        return WorkerResponse.from(w);
    }

    @PostMapping("/{workerId}/heartbeat")
    public WorkerResponse heartbeat(@PathVariable UUID workerId) {
        return WorkerResponse.from(workers.heartbeat(workerId));
    }
}
