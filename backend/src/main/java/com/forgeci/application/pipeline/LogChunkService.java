package com.forgeci.application.pipeline;

import com.forgeci.domain.pipeline.LogChunk;
import com.forgeci.infrastructure.pipeline.LogChunkRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogChunkService {
    private static final int MAX_CHUNK=16*1024;
    private static final long MAX_LOG=10L*1024*1024;
    private final LogChunkRepository repository;
    private final SimpMessagingTemplate messaging;

    public LogChunkService(LogChunkRepository repository,SimpMessagingTemplate messaging){this.repository=repository;this.messaging=messaging;}

    public long nextSequence(UUID jobRunId){return repository.nextSequence(jobRunId);}

    @Transactional
    public synchronized void append(UUID jobRunId,String stream,long sequence,String content){
        if(content==null||content.isEmpty())return;
        String remaining=content; long next=sequence;
        while(!remaining.isEmpty()){
            long stored=repository.totalBytes(jobRunId);
            if(stored>=MAX_LOG)return;
            String chunk=remaining.substring(0,Math.min(MAX_CHUNK,remaining.length()));
            if(stored+chunk.length()>MAX_LOG)chunk=chunk.substring(0,(int)(MAX_LOG-stored));
            if(chunk.isEmpty())return;
            repository.save(new LogChunk(jobRunId,next++,stream,chunk,Instant.now()));
            messaging.convertAndSend("/topic/jobs/"+jobRunId+"/logs",chunk);
            remaining=remaining.substring(chunk.length());
        }
    }

    @Transactional(readOnly=true)
    public List<LogChunk> list(UUID jobRunId,int tail,Long after){
        if(tail<=0||tail>5000)throw new IllegalArgumentException("tail must be between 1 and 5000");
        return after==null?repository.findTail(jobRunId,tail):repository.findAfter(jobRunId,after,PageRequest.of(0,tail));
    }
}
