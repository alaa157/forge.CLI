package com.forgeci.application.pipeline;

import com.forgeci.config.PipelineDispatchRabbitConfig;
import com.forgeci.domain.pipeline.*;
import com.forgeci.infrastructure.pipeline.PipelineDispatchRepository;
import java.util.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PipelineDispatchPublisher {
    private final PipelineDispatchRepository dispatches;
    private final RabbitTemplate rabbitTemplate;

    public PipelineDispatchPublisher(
            PipelineDispatchRepository dispatches,
            RabbitTemplate rabbitTemplate) {
        this.dispatches = dispatches;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelayString = "${forgeci.pipeline.publisher-delay-ms:500}")
    public void publishPending() {
        for (PipelineDispatch d : dispatches.findTop50ByStatusOrderByCreatedAtAsc(PipelineDispatchStatus.PENDING)) {
            publish(d);
        }
    }

    @Transactional
    void publish(PipelineDispatch d) {
        d.recordPublishAttempt();
        try {
            rabbitTemplate.convertAndSend(
                    PipelineDispatchRabbitConfig.EXCHANGE,
                    PipelineDispatchRabbitConfig.ROUTING_KEY,
                    new PipelineDispatchMessage(d.getId(), d.getId(), d.getPipelineRunId()));
            d.markPublished();
        } catch (RuntimeException e) {
            d.markFailed(e.getMessage());
        }
        dispatches.save(d);
    }
}
