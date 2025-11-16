package com.example.chatapp.web.websocket;

import com.example.chatapp.service.dto.kafka.RoomEvent;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/testWS")
public class TestWSresource {

    private final SimpMessageSendingOperations messagingTemplate;

    public TestWSresource(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("")
    @SendToUser("/queue/room-event")
    public void test(@RequestBody @Payload RoomEvent event) {
        event
            .getRecipients()
            .forEach(recipient -> {
                messagingTemplate.convertAndSendToUser(recipient, "/queue/room-event", event);
            });
    }
}
