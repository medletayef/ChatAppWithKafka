package com.example.chatapp.service.kafka.userStatus;

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
import org.springframework.stereotype.Service;

@Service
public class UserStatusConsumer {

    private final SimpMessageSendingOperations messagingTemplate;
    private final RedisTemplate<String, String> redisTemplate; // or use StringRedisTemplate
    private final Logger log = LoggerFactory.getLogger(UserStatusConsumer.class);

    @Value("${app.user-status.ttl-seconds:90}")
    private long ttlSeconds;

    public UserStatusConsumer(SimpMessagingTemplate messagingTemplate, StringRedisTemplate redisTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "${app.kafka.topic.user-status:user-status}", groupId = "user-status-group")
    public void consume(UserStatusEvent event) {
        log.debug("Consumed user status: {}", event);
        String key = "presence:" + event.getUserId();
        // store JSON or simple state
        redisTemplate.opsForValue().set(key, event.getState().name(), ttlSeconds, TimeUnit.SECONDS);

        // Broadcast to all connected UIs
        messagingTemplate.convertAndSend("/topic/user-status", event);
    }
}
