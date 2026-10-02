package com.lms.listener;

import com.lms.dto.event.UserRegisteredEvent;
import com.lms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserRegisteredListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${app.rabbit.notification-queue}")
    public void handle(UserRegisteredEvent event) {
        // ВНИМАНИЕ: этот листенер получит ВСЕ события из очереди (все 3 типа),
        // поэтому реальная маршрутизация по типу происходит в главном роутере — см. NotificationEventRouter ниже.
    }
}