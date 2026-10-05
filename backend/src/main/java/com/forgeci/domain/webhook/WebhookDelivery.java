package com.forgeci.domain.webhook;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="webhook_deliveries",schema="forgeci",uniqueConstraints=@UniqueConstraint(name="uk_webhook_delivery_provider_id",columnNames={"provider","delivery_id"}))
public class WebhookDelivery {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(nullable=false,length=30) private String provider; @Column(name="delivery_id",nullable=false,length=128) private String deliveryId;
 @Column(name="event_type",nullable=false,length=50) private String eventType; @Column(name="received_at",nullable=false) private Instant receivedAt;
 @Column(name="processed_at") private Instant processedAt; @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private WebhookDeliveryStatus status;
 @Column(name="payload_hash",nullable=false,length=64) private String payloadHash;
 protected WebhookDelivery() {}
 public WebhookDelivery(String provider,String deliveryId,String eventType,Instant receivedAt,String payloadHash){
  if(provider==null||provider.isBlank())throw new IllegalArgumentException("provider is required");
  if(deliveryId==null||deliveryId.isBlank())throw new IllegalArgumentException("deliveryId is required");
  if(eventType==null||eventType.isBlank())throw new IllegalArgumentException("eventType is required");
  if(receivedAt==null)throw new IllegalArgumentException("receivedAt is required");
  if(payloadHash==null||payloadHash.length()!=64)throw new IllegalArgumentException("payloadHash must be SHA-256");
  this.provider=provider;this.deliveryId=deliveryId;this.eventType=eventType;this.receivedAt=receivedAt;this.payloadHash=payloadHash;this.status=WebhookDeliveryStatus.RECEIVED;
 }
 public void markProcessed(Instant now){status=WebhookDeliveryStatus.PROCESSED;processedAt=now;}
 public void markFailed(Instant now){status=WebhookDeliveryStatus.FAILED;processedAt=now;}
 public UUID getId(){return id;} public String getProvider(){return provider;} public String getDeliveryId(){return deliveryId;} public String getEventType(){return eventType;}
 public Instant getReceivedAt(){return receivedAt;} public Instant getProcessedAt(){return processedAt;} public WebhookDeliveryStatus getStatus(){return status;} public String getPayloadHash(){return payloadHash;}
}