package com.lms.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String LMS_EXCHANGE = "lms-exchange";

    public static final String COURSE_DELETE_QUEUE = "course-delete-queue";
    public static final String COURSE_DELETE_ROUTING_KEY = "course.deleted.key";

    public static final String CERTIFICATE_QUEUE = "certificate-issue-queue";
    public static final String QUIZ_PASSED_ROUTING_KEY = "quiz.passed.key";

    @Bean
    public TopicExchange lmsExchange() {
        return new TopicExchange(LMS_EXCHANGE);
    }

    @Bean
    public Queue courseDeleteQueue() {
        return new Queue(COURSE_DELETE_QUEUE, true);
    }

    @Bean
    public Queue certificateQueue() {
        return QueueBuilder.durable(CERTIFICATE_QUEUE).build();
    }

    @Bean
    public Binding courseDeleteBinding(Queue courseDeleteQueue, TopicExchange lmsExchange) {
        return BindingBuilder.bind(courseDeleteQueue)
                .to(lmsExchange)
                .with(COURSE_DELETE_ROUTING_KEY);
    }

    @Bean
    public Binding certificateBinding(Queue certificateQueue, TopicExchange lmsExchange) {
        return BindingBuilder.bind(certificateQueue)
                .to(lmsExchange)
                .with(QUIZ_PASSED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}