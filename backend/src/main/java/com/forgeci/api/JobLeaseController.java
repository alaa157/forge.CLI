package com.forgeci.api;

import com.forgeci.application.pipeline.JobLeaseService;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/jobs")
public class JobLeaseController {
    private final JobLeaseService leases;

    public JobLeaseController(JobLeaseService leases) {
        this.leases = leases;
    }

    public record AcquireRequest(UUID workerId) {}
    public record RenewRequest(UUID leaseId, UUID workerId) {}

    @PostMapping("/{jobRunId}/lease")
    public JobLeaseService.Lease acquire(@PathVariable UUID jobRunId, @RequestBody AcquireRequest body) {
        return leases.acquire(jobRunId, body.workerId());
    }

    @PostMapping("/{jobRunId}/lease/renew")
    public void renew(@PathVariable UUID jobRunId, @RequestBody RenewRequest body) {
        leases.renew(jobRunId, body.leaseId(), body.workerId());
    }
}
