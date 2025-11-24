package com.example.chatapp.web.websocket;

import com.example.chatapp.service.dto.kafka.UserStatusEvent;
import com.example.chatapp.service.kafka.userStatus.UserStatusProducer;
import java.security.Principal;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@Controller
public class PresenceController {

    private final UserStatusProducer producer;

    public PresenceController(UserStatusProducer producer) {
        this.producer = producer;
    }

    @MessageMapping("/topic/presence")
    public void presence(PresencePayload payload, Principal principal, @Header("simpSessionId") String sessionId) {
        if (principal == null) return;
        UserStatusEvent.State state = payload.getState(); // ACTIVE or ABSENT
        producer.publish(principal.getName(), state, sessionId);
    }

    public static class PresencePayload {

        private UserStatusEvent.State state;

        public UserStatusEvent.State getState() {
            return state;
        }

        public void setState(UserStatusEvent.State state) {
            this.state = state;
        }
        // getter/setter
    }
}
