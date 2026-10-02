package com.lms.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String CERTIFICATE_QUEUE = "certificate-issue-queue";
    public static final String LMS_EXCHANGE = "lms-exchange";
    public static final String QUIZ_PASSED_ROUTING_KEY = "quiz.passed.key";

    @Value("${app.rabbit.notification-exchange}")
    private String notificationExchangeName;

    // ---- существующее: слушаем quiz.passed для выдачи сертификатов ----

    @Bean
    public TopicExchange quizExchange() {
        return new TopicExchange(LMS_EXCHANGE);
    }

    @Bean
    public Queue certificateQueue() {
        return QueueBuilder.durable(CERTIFICATE_QUEUE).build();
    }

    @Bean
    public Binding certificateBinding(Queue certificateQueue, TopicExchange quizExchange) {
        return BindingBuilder.bind(certificateQueue)
                .to(quizExchange)
                .with(QUIZ_PASSED_ROUTING_KEY);
    }

    // ---- новое: публикуем в notification.exchange после выдачи сертификата ----

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(notificationExchangeName);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}