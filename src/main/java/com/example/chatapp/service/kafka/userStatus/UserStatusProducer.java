package com.example.chatapp.service.kafka.userStatus;

import com.example.chatapp.domain.User;
import com.example.chatapp.service.UserService;
import com.example.chatapp.service.dto.kafka.UserStatusEvent;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserStatusProducer {

    private final KafkaTemplate<String, UserStatusEvent> kafkaTemplate;

    private final UserService userService;

    @Value("${app.kafka.topic.user-status:user-status}")
    private String topic;

    public UserStatusProducer(KafkaTemplate<String, UserStatusEvent> kafkaTemplate, UserService userService) {
        this.kafkaTemplate = kafkaTemplate;
        this.userService = userService;
    }

    public void publish(String userId, UserStatusEvent.State state, String sessionId) {
        UserStatusEvent evt = new UserStatusEvent(userId, state, Instant.now(), sessionId);
        User current = userService.getUserWithAuthoritiesByLogin(userId).orElseThrow();
        evt.setFullName(current.getFirstName() + " " + current.getLastName());
        evt.setImageUrl(current.getImageUrl());
        kafkaTemplate.send(topic, userId, evt);
    }
}
