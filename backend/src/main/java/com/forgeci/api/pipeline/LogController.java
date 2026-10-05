package com.forgeci.api.pipeline;

import com.forgeci.application.pipeline.LogChunkService;
import com.forgeci.domain.pipeline.LogChunk;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jobs")
public class LogController {
    private final LogChunkService logs;
    public LogController(LogChunkService logs){this.logs=logs;}

    @GetMapping("/{id}/logs")
    public List<LogChunk> logs(@PathVariable UUID id,
                               @RequestParam(defaultValue="500") int tail,
                               @RequestParam(required=false) Long after,
                               @RequestParam(required=false)
                               @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant since){
        return logs.list(id,tail,after,since);
    }
}
