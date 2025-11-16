package com.example.chatapp.service.kafka.room;

import com.example.chatapp.service.dto.kafka.RoomEvent;
import com.example.chatapp.service.dto.kafka.UserStatusEvent;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Service;

@Service
@SendToUser("/queue/room-event")
public class RoomEventConsumer {

    private final SimpMessageSendingOperations messagingTemplate;
    private final Logger log = LoggerFactory.getLogger(RoomEventConsumer.class);

    public RoomEventConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @SendToUser("/queue/room-event")
    @KafkaListener(topics = "${app.kafka.topic.room-events:room-events}", groupId = "room-event-group")
    public void consume(RoomEvent event) {
        log.debug("Consumed room event: {}", event);
        event.getRecipients().forEach(recipient -> messagingTemplate.convertAndSendToUser(recipient, "/queue/room-event", event));
    }
}
