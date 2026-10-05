package com.forgeci.domain.webhook;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;
class WebhookDeliveryTest {
 @Test void startsReceivedAndCanBeProcessed(){
  var delivery=new WebhookDelivery("GITHUB","delivery-1","push",Instant.now(),"a".repeat(64));
  assertEquals(WebhookDeliveryStatus.RECEIVED,delivery.getStatus());
  var processed=Instant.now(); delivery.markProcessed(processed);
  assertEquals(WebhookDeliveryStatus.PROCESSED,delivery.getStatus()); assertEquals(processed,delivery.getProcessedAt());
 }
 @Test void canBeMarkedFailedForRetry(){
  var delivery=new WebhookDelivery("GITHUB","delivery-2","push",Instant.now(),"b".repeat(64));
  delivery.markFailed(Instant.now()); assertEquals(WebhookDeliveryStatus.FAILED,delivery.getStatus());
 }
}