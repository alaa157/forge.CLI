package com.forgeci.domain.pipeline;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="pipeline_dispatches",schema="forgeci",
 indexes={@Index(name="idx_pipeline_dispatches_status",columnList="status"),@Index(name="idx_pipeline_dispatches_created_at",columnList="created_at")},
 uniqueConstraints=@UniqueConstraint(name="uk_pipeline_dispatch_run",columnNames="pipeline_run_id"))
public class PipelineDispatch {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(name="pipeline_run_id",nullable=false) private UUID pipelineRunId;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private PipelineDispatchStatus status;
 @Column(nullable=false) private int attempts;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @Column(name="published_at") private Instant publishedAt;
 @Column(name="last_error",length=2000) private String lastError;
 protected PipelineDispatch(){}
 public PipelineDispatch(UUID pipelineRunId){if(pipelineRunId==null)throw new IllegalArgumentException("pipelineRunId is required");this.pipelineRunId=pipelineRunId;this.status=PipelineDispatchStatus.PENDING;}
 @PrePersist void onCreate(){createdAt=Instant.now();}
 public void recordPublishAttempt(){attempts++;}
 public void markPublished(){if(status!=PipelineDispatchStatus.PENDING)throw new IllegalStateException("Dispatch is not pending");status=PipelineDispatchStatus.PUBLISHED;publishedAt=Instant.now();lastError=null;}
 public void markFailed(String error){if(status!=PipelineDispatchStatus.PENDING)throw new IllegalStateException("Dispatch is not pending");lastError=error==null?"Unknown dispatch failure":error.substring(0,Math.min(error.length(),2000));}
 public UUID getId(){return id;} public UUID getPipelineRunId(){return pipelineRunId;} public PipelineDispatchStatus getStatus(){return status;} public int getAttempts(){return attempts;} public Instant getCreatedAt(){return createdAt;} public Instant getPublishedAt(){return publishedAt;} public String getLastError(){return lastError;}
}
