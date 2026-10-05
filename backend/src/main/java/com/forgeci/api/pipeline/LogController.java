package com.forgeci.api.pipeline;

import com.forgeci.application.pipeline.LogChunkService;
import com.forgeci.domain.pipeline.LogChunk;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/jobs")
public class LogController {
    private final LogChunkService logs;
    public LogController(LogChunkService logs){this.logs=logs;}

    @GetMapping("/{id}/logs")
    public List<LogChunk> logs(@PathVariable UUID id,@RequestParam(defaultValue="500") int tail,@RequestParam(required=false) Long after){
        return logs.list(id,tail,after);
    }
}
