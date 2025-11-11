package com.example.chatapp.service.kafka.userStatus;

import com.example.chatapp.service.dto.kafka.UserStatusEvent;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private final UserStatusProducer producer;
    private final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    public WebSocketEventListener(UserStatusProducer producer) {
        this.producer = producer;
    }

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        // extract user/principal and sessionId
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = sha.getUser();
        String sessionId = sha.getSessionId();
        if (principal != null) {
            String userId = principal.getName(); // typically login
            log.debug("WS connected user={}, session={}", userId, sessionId);
            producer.publish(userId, UserStatusEvent.State.ACTIVE, sessionId);
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = sha.getUser();
        String sessionId = sha.getSessionId();
        if (principal != null) {
            String userId = principal.getName();
            log.debug("WS disconnected user={}, session={}", userId, sessionId);
            producer.publish(userId, UserStatusEvent.State.OFFLINE, sessionId);
        }
    }
}
