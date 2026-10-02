package com.lms.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${app.rabbit.notification-exchange}")
    private String notificationExchange;

    @Value("${app.rabbit.notification-queue}")
    private String notificationQueue;

    @Value("${app.rabbit.notification-dlq}")
    private String notificationDlq;

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(notificationExchange);
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(notificationDlq).build();
    }

    @Bean
    public Queue notificationQueue() {
        // При исчерпании retry (см. application.yml) сообщение уходит в DLQ через x-dead-letter-exchange
        return QueueBuilder.durable(notificationQueue)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", notificationDlq)
                .build();
    }

    // Одна очередь слушает сразу три routing key — разные типы событий
    @Bean
    public Binding userRegisteredBinding(Queue notificationQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with("user.registered");
    }

    @Bean
    public Binding enrollmentCreatedBinding(Queue notificationQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with("enrollment.created");
    }

    @Bean
    public Binding certificateIssuedBinding(Queue notificationQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with("certificate.issued");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}