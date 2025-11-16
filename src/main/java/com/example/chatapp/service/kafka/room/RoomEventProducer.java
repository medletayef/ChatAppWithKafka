package com.example.chatapp.service.kafka.room;

import com.example.chatapp.config.kafka.KafkaConfig;
import com.example.chatapp.domain.User;
import com.example.chatapp.service.UserService;
import com.example.chatapp.service.dto.kafka.RoomEvent;
import com.example.chatapp.service.dto.kafka.UserStatusEvent;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class RoomEventProducer {

    private final KafkaTemplate<String, RoomEvent> kafkaTemplate;

    private final Logger log = LoggerFactory.getLogger(RoomEventProducer.class);

    @Value("${app.kafka.topic.room-events:room-events}")
    private String topic;

    public RoomEventProducer(KafkaTemplate<String, RoomEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(RoomEvent event) {
        kafkaTemplate.send(topic, event.getRoomId().toString(), event);
        log.info("📤 Sent RoomEvent: type={}, topic={}", event.getType(), topic + event.getRoomId());
    }
}
