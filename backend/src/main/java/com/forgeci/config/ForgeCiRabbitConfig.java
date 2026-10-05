package com.forgeci.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ForgeCiRabbitConfig {
    public static final String EXCHANGE = "forgeci.messaging";

    public static final String JOBS_QUEUE = "forgeci.jobs";
    public static final String EVENTS_QUEUE = "forgeci.events";
    public static final String LOGS_QUEUE = "forgeci.logs";
    public static final String NOTIFICATIONS_QUEUE = "forgeci.notifications";

    public static final String JOBS_DLQ = "forgeci.jobs.dlq";
    public static final String EVENTS_DLQ = "forgeci.events.dlq";
    public static final String LOGS_DLQ = "forgeci.logs.dlq";

    public static final String JOBS_ROUTING_KEY = "jobs";
    public static final String EVENTS_ROUTING_KEY = "events";
    public static final String LOGS_ROUTING_KEY = "logs";
    public static final String NOTIFICATIONS_ROUTING_KEY = "notifications";

    public static final String JOBS_DLQ_ROUTING_KEY = "jobs.dlq";
    public static final String EVENTS_DLQ_ROUTING_KEY = "events.dlq";
    public static final String LOGS_DLQ_ROUTING_KEY = "logs.dlq";

    @Bean
    DirectExchange forgeCiMessagingExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean(name = "forgeCiJobsQueue")
    Queue forgeCiJobsQueue() {
        return QueueBuilderSupport.durable(JOBS_QUEUE).deadLetterExchange(EXCHANGE)
                .deadLetterRoutingKey(JOBS_DLQ_ROUTING_KEY).build();
    }

    @Bean(name = "forgeCiEventsQueue")
    Queue forgeCiEventsQueue() {
        return QueueBuilderSupport.durable(EVENTS_QUEUE).deadLetterExchange(EXCHANGE)
                .deadLetterRoutingKey(EVENTS_DLQ_ROUTING_KEY).build();
    }

    @Bean(name = "forgeCiLogsQueue")
    Queue forgeCiLogsQueue() {
        return QueueBuilderSupport.durable(LOGS_QUEUE).deadLetterExchange(EXCHANGE)
                .deadLetterRoutingKey(LOGS_DLQ_ROUTING_KEY).build();
    }

    @Bean(name = "forgeCiNotificationsQueue")
    Queue forgeCiNotificationsQueue() {
        return QueueBuilderSupport.durable(NOTIFICATIONS_QUEUE).build();
    }

    @Bean(name = "forgeCiJobsDlq")
    Queue forgeCiJobsDlq() {
        return QueueBuilderSupport.durable(JOBS_DLQ).build();
    }

    @Bean(name = "forgeCiEventsDlq")
    Queue forgeCiEventsDlq() {
        return QueueBuilderSupport.durable(EVENTS_DLQ).build();
    }

    @Bean(name = "forgeCiLogsDlq")
    Queue forgeCiLogsDlq() {
        return QueueBuilderSupport.durable(LOGS_DLQ).build();
    }

    @Bean
    Binding forgeCiJobsBinding(Queue forgeCiJobsQueue, DirectExchange forgeCiMessagingExchange) {
        return BindingBuilder.bind(forgeCiJobsQueue).to(forgeCiMessagingExchange).with(JOBS_ROUTING_KEY);
    }

    @Bean
    Binding forgeCiEventsBinding(Queue forgeCiEventsQueue, DirectExchange forgeCiMessagingExchange) {
        return BindingBuilder.bind(forgeCiEventsQueue).to(forgeCiMessagingExchange).with(EVENTS_ROUTING_KEY);
    }

    @Bean
    Binding forgeCiLogsBinding(Queue forgeCiLogsQueue, DirectExchange forgeCiMessagingExchange) {
        return BindingBuilder.bind(forgeCiLogsQueue).to(forgeCiMessagingExchange).with(LOGS_ROUTING_KEY);
    }

    @Bean
    Binding forgeCiNotificationsBinding(Queue forgeCiNotificationsQueue, DirectExchange forgeCiMessagingExchange) {
        return BindingBuilder.bind(forgeCiNotificationsQueue).to(forgeCiMessagingExchange).with(NOTIFICATIONS_ROUTING_KEY);
    }

    @Bean
    Binding forgeCiJobsDlqBinding(Queue forgeCiJobsDlq, DirectExchange forgeCiMessagingExchange) {
        return BindingBuilder.bind(forgeCiJobsDlq).to(forgeCiMessagingExchange).with(JOBS_DLQ_ROUTING_KEY);
    }

    @Bean
    Binding forgeCiEventsDlqBinding(Queue forgeCiEventsDlq, DirectExchange forgeCiMessagingExchange) {
        return BindingBuilder.bind(forgeCiEventsDlq).to(forgeCiMessagingExchange).with(EVENTS_DLQ_ROUTING_KEY);
    }

    @Bean
    Binding forgeCiLogsDlqBinding(Queue forgeCiLogsDlq, DirectExchange forgeCiMessagingExchange) {
        return BindingBuilder.bind(forgeCiLogsDlq).to(forgeCiMessagingExchange).with(LOGS_DLQ_ROUTING_KEY);
    }

    @Bean
    Jackson2JsonMessageConverter forgeCiMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    private static final class QueueBuilderSupport {
        private static org.springframework.amqp.core.QueueBuilder durable(String name) {
            return org.springframework.amqp.core.QueueBuilder.durable(name);
        }
    }
}
