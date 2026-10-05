package com.forgeci.infrastructure.webhook;
import com.forgeci.domain.webhook.WebhookDelivery; import com.forgeci.domain.webhook.WebhookDeliveryStatus; import jakarta.persistence.LockModeType;
import java.time.Instant; import java.util.Optional; import java.util.UUID; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import org.springframework.stereotype.Repository;
@Repository public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery,UUID>{
 @Modifying @Query(value="INSERT INTO forgeci.webhook_deliveries (id,provider,delivery_id,event_type,received_at,status,payload_hash) VALUES (:id,:provider,:deliveryId,:eventType,:receivedAt,'RECEIVED',:payloadHash) ON CONFLICT (provider,delivery_id) DO NOTHING",nativeQuery=true)
 int claim(@Param("id") UUID id,@Param("provider") String provider,@Param("deliveryId") String deliveryId,@Param("eventType") String eventType,@Param("receivedAt") Instant receivedAt,@Param("payloadHash") String payloadHash);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select d from WebhookDelivery d where d.provider=:provider and d.deliveryId=:deliveryId")
 Optional<WebhookDelivery> findForUpdate(@Param("provider") String provider,@Param("deliveryId") String deliveryId);
 long countByStatus(WebhookDeliveryStatus status);
}