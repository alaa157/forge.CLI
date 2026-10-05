package com.forgeci.domain.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PipelineDispatchTest {
 @Test void startsPendingAndRecordsAttempts(){
  PipelineDispatch d=new PipelineDispatch(UUID.randomUUID());
  assertEquals(PipelineDispatchStatus.PENDING,d.getStatus());
  d.recordPublishAttempt();
  assertEquals(1,d.getAttempts());
 }
 @Test void publishesOnlyFromPending(){
  PipelineDispatch d=new PipelineDispatch(UUID.randomUUID());
  d.markPublished();
  assertEquals(PipelineDispatchStatus.PUBLISHED,d.getStatus());
  assertNotNull(d.getPublishedAt());
  assertThrows(IllegalStateException.class,d::markPublished);
 }
 @Test void truncatesFailureMessage(){
  PipelineDispatch d=new PipelineDispatch(UUID.randomUUID());
  d.markFailed("x".repeat(3000));
  assertEquals(2000,d.getLastError().length());
 }
}
