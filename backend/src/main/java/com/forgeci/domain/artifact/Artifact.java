package com.forgeci.domain.artifact;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="artifacts",schema="forgeci",indexes={
 @Index(name="idx_artifacts_job",columnList="job_run_id"),
 @Index(name="idx_artifacts_created",columnList="created_at")
})
public class Artifact {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(name="job_run_id",nullable=false) private UUID jobRunId;
 @Column(nullable=false,length=255) private String name;
 @Column(name="object_key",nullable=false,length=1024,unique=true) private String objectKey;
 @Column(name="size_bytes",nullable=false) private long sizeBytes;
 @Column(nullable=false,length=64) private String sha256;
 @Column(name="content_type",nullable=false,length=255) private String contentType;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 protected Artifact(){}
 public Artifact(UUID jobRunId,String name,String objectKey,long sizeBytes,String sha256,String contentType){
  if(jobRunId==null)throw new IllegalArgumentException("jobRunId is required");
  if(name==null||name.isBlank())throw new IllegalArgumentException("name is required");
  if(objectKey==null||objectKey.isBlank())throw new IllegalArgumentException("objectKey is required");
  if(sizeBytes<0)throw new IllegalArgumentException("sizeBytes must be non-negative");
  this.jobRunId=jobRunId;this.name=name;this.objectKey=objectKey;this.sizeBytes=sizeBytes;this.sha256=sha256;this.contentType=contentType;
 }
 @PrePersist void onCreate(){createdAt=Instant.now();}
 public UUID getId(){return id;} public UUID getJobRunId(){return jobRunId;} public String getName(){return name;}
 public String getObjectKey(){return objectKey;} public long getSizeBytes(){return sizeBytes;} public String getSha256(){return sha256;}
 public String getContentType(){return contentType;} public Instant getCreatedAt(){return createdAt;}
}