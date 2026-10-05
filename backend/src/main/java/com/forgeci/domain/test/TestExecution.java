package com.forgeci.domain.test;
import jakarta.persistence.*;import java.time.Instant;import java.util.UUID;
@Entity @Table(name="test_executions",schema="forgeci",indexes={
 @Index(name="idx_test_exec_test",columnList="test_id,executed_at"),
 @Index(name="idx_test_exec_job",columnList="job_run_id"),
 @Index(name="idx_test_exec_repo",columnList="repository_id")
})
public class TestExecution{
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(name="test_id",nullable=false,length=512) private String testId;
 @Column(name="job_run_id",nullable=false) private UUID jobRunId;
 @Column(name="repository_id",nullable=false) private UUID repositoryId;
 @Column(nullable=false,length=255) private String suite;
 @Column(name="class_name",nullable=false,length=512) private String className;
 @Column(name="test_name",nullable=false,length=512) private String testName;
 @Column(nullable=false,length=64) private String framework;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=16) private TestStatus status;
 @Column(name="duration_ms",nullable=false) private long durationMs;
 @Column(name="failure_message",columnDefinition="text") private String failureMessage;
 @Column(columnDefinition="text") private String stdout;
 @Column(columnDefinition="text") private String stderr;
 @Column(name="commit_sha",nullable=false,length=64) private String commitSha;
 @Column(nullable=false,length=255) private String branch;
 @Column(name="executed_at",nullable=false) private Instant executedAt;
 protected TestExecution(){}
 public TestExecution(String testId,UUID jobRunId,UUID repositoryId,String suite,String className,String testName,String framework,TestStatus status,long durationMs,String failureMessage,String stdout,String stderr,String commitSha,String branch,Instant executedAt){
  if(testId==null||testId.isBlank())throw new IllegalArgumentException("testId is required");if(jobRunId==null||repositoryId==null)throw new IllegalArgumentException("job and repository are required");
  this.testId=testId;this.jobRunId=jobRunId;this.repositoryId=repositoryId;this.suite=suite==null?"":suite;this.className=className==null?"":className;this.testName=testName==null?"":testName;this.framework=framework;this.status=status;this.durationMs=Math.max(0,durationMs);this.failureMessage=failureMessage;this.stdout=stdout;this.stderr=stderr;this.commitSha=commitSha;this.branch=branch;this.executedAt=executedAt==null?Instant.now():executedAt;
 }
 public UUID getId(){return id;} public String getTestId(){return testId;} public UUID getJobRunId(){return jobRunId;} public UUID getRepositoryId(){return repositoryId;} public String getSuite(){return suite;} public String getClassName(){return className;} public String getTestName(){return testName;} public String getFramework(){return framework;} public TestStatus getStatus(){return status;} public long getDurationMs(){return durationMs;} public String getFailureMessage(){return failureMessage;} public String getStdout(){return stdout;} public String getStderr(){return stderr;} public String getCommitSha(){return commitSha;} public String getBranch(){return branch;} public Instant getExecutedAt(){return executedAt;}
}