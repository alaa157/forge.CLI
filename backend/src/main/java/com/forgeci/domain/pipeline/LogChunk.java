package com.forgeci.domain.pipeline;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="log_chunks", schema="forgeci",
       uniqueConstraints=@UniqueConstraint(name="uk_log_chunk_sequence", columnNames={"job_run_id","sequence"}))
public class LogChunk {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(name="job_run_id",nullable=false) private UUID jobRunId;
    @Column(name="sequence",nullable=false) private long sequence;
    @Column(name="stream",nullable=false,length=16) private String stream;
    @Column(nullable=false,length=16384) private String content;
    @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    protected LogChunk() {}
    public LogChunk(UUID jobRunId,long sequence,String stream,String content,Instant createdAt) {
        if(jobRunId==null) throw new IllegalArgumentException("jobRunId is required");
        if(sequence<0) throw new IllegalArgumentException("sequence must be >= 0");
        if(!"stdout".equals(stream)&&!"stderr".equals(stream)) throw new IllegalArgumentException("invalid stream");
        if(content==null||content.isEmpty()||content.length()>16384) throw new IllegalArgumentException("invalid content");
        this.jobRunId=jobRunId; this.sequence=sequence; this.stream=stream; this.content=content; this.createdAt=createdAt==null?Instant.now():createdAt;
    }
    public UUID getId(){return id;} public UUID getJobRunId(){return jobRunId;} public long getSequence(){return sequence;} public String getStream(){return stream;} public String getContent(){return content;} public Instant getCreatedAt(){return createdAt;}
}
