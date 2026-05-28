package com.lms.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    public static final String REGISTRATION_QUEUE = "user-registration-queue";
    public static final String LMS_EXCHANGE = "lms-exchange";
    public static final String REGISTRATION_ROUTING_KEY = "user.registration.key";

    @Bean
    public Queue registrationQueue() {
        return new Queue(REGISTRATION_QUEUE, true);
    }

    @Bean
    public TopicExchange lmsExchange() {
        return new TopicExchange(LMS_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue registrationQueue, TopicExchange lmsExchange) {
        return BindingBuilder.bind(registrationQueue).to(lmsExchange).with(REGISTRATION_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
