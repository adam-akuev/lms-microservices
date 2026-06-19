package com.lms.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    public static final String STUDENT_QUEUE = "student-registration-queue";
    public static final String TEACHER_QUEUE = "teacher-registration-queue";

    public static final String LMS_EXCHANGE = "lms-exchange";

    public static final String STUDENT_ROUTING_KEY = "user.registration.student";
    public static final String TEACHER_ROUTING_KEY = "user.registration.teacher";

    @Bean
    public Queue studentQueue() {
        return new Queue(STUDENT_QUEUE, true);
    }

    @Bean
    public Queue teacherQueue() {
        return new Queue(TEACHER_QUEUE, true);
    }

    @Bean
    public TopicExchange lmsExchange() {
        return new TopicExchange(LMS_EXCHANGE);
    }

    @Bean
    public Binding studentBinding(Queue studentQueue, TopicExchange lmsExchange) {
        return BindingBuilder.bind(studentQueue).to(lmsExchange).with(STUDENT_ROUTING_KEY);
    }

    @Bean
    public Binding teacherBinding(Queue teacherQueue, TopicExchange lmsExchange) {
        return BindingBuilder.bind(teacherQueue).to(lmsExchange).with(TEACHER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
