package com.forgeci.domain.pipeline;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "step_runs", schema = "forgeci",
       indexes = @Index(name = "idx_step_runs_job", columnList = "job_run_id"))
public class StepRun {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="job_run_id",nullable=false)
    private UUID jobRunId;

    @Column(nullable=false)
    private int position;

    @Column(nullable=false,length=8192)
    private String command;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false,length=20)
    private StepRunStatus status;

    @Column(name="created_at",nullable=false,updatable=false)
    private Instant createdAt;

    @Column(name="started_at")
    private Instant startedAt;

    @Column(name="finished_at")
    private Instant finishedAt;

    protected StepRun() {}

    public StepRun(UUID jobRunId,int position,String command) {
        if(jobRunId==null) throw new IllegalArgumentException("jobRunId is required");
        if(position<0) throw new IllegalArgumentException("position must be >= 0");
        if(command==null||command.isBlank()) throw new IllegalArgumentException("command is required");
        if(command.length()>8192) throw new IllegalArgumentException("command is too long");
        this.jobRunId=jobRunId; this.position=position; this.command=command; this.status=StepRunStatus.PENDING;
    }

    @PrePersist void onCreate(){createdAt=Instant.now();}
    public void start(){transition(StepRunStatus.RUNNING);startedAt=Instant.now();}
    public void succeed(){transition(StepRunStatus.SUCCEEDED);finishedAt=Instant.now();}
    public void fail(){transition(StepRunStatus.FAILED);finishedAt=Instant.now();}
    public void cancel(){transition(StepRunStatus.CANCELLED);finishedAt=Instant.now();}
    public void timeOut(){transition(StepRunStatus.TIMED_OUT);finishedAt=Instant.now();}
    public void skip(){transition(StepRunStatus.SKIPPED);finishedAt=Instant.now();}
    public void resetForRetry(){ if(status==StepRunStatus.PENDING) return; status=StepRunStatus.PENDING; startedAt=null; finishedAt=null; }
    private void transition(StepRunStatus target){if(status!=StepRunStatus.PENDING&&status!=StepRunStatus.RUNNING)throw new IllegalStateException("Invalid step run transition: "+status+" -> "+target); if(status==StepRunStatus.PENDING&&target!=StepRunStatus.RUNNING&&target!=StepRunStatus.SKIPPED&&target!=StepRunStatus.CANCELLED)throw new IllegalStateException("Invalid step run transition: "+status+" -> "+target); if(status==StepRunStatus.RUNNING&&(target!=StepRunStatus.SUCCEEDED&&target!=StepRunStatus.FAILED&&target!=StepRunStatus.CANCELLED&&target!=StepRunStatus.TIMED_OUT))throw new IllegalStateException("Invalid step run transition: "+status+" -> "+target);status=target;}
    public UUID getId(){return id;} public UUID getJobRunId(){return jobRunId;} public int getPosition(){return position;} public String getCommand(){return command;} public StepRunStatus getStatus(){return status;} public Instant getCreatedAt(){return createdAt;} public Instant getStartedAt(){return startedAt;} public Instant getFinishedAt(){return finishedAt;}
}