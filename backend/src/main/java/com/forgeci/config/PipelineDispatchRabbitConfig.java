package com.forgeci.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.*;

@Configuration
public class PipelineDispatchRabbitConfig {
 public static final String EXCHANGE="forgeci.pipeline";
 public static final String QUEUE="forgeci.pipeline.dispatch";
 public static final String ROUTING_KEY="pipeline.dispatch";
 @Bean DirectExchange pipelineExchange(){return new DirectExchange(EXCHANGE,true,false);}
 @Bean Queue pipelineDispatchQueue(){return QueueBuilder.durable(QUEUE).build();}
 @Bean Binding pipelineDispatchBinding(Queue q,DirectExchange e){return BindingBuilder.bind(q).to(e).with(ROUTING_KEY);}
 @Bean Jackson2JsonMessageConverter pipelineMessageConverter(){return new Jackson2JsonMessageConverter();}
}
