package com.example.chatapp.service.kafka.message;

import com.example.chatapp.repository.ChatRoomRepository;
import com.example.chatapp.service.ChatRoomService;
import com.example.chatapp.service.UserService;
import com.example.chatapp.service.dto.ChatRoomDTO;
import com.example.chatapp.service.dto.kafka.MessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Service;

@Service
public class MessageEventConsumer {

    private final SimpMessageSendingOperations messagingTemplate;

    private final ChatRoomService chatRoomService;

    private final Logger log = LoggerFactory.getLogger(MessageEventConsumer.class);

    public MessageEventConsumer(
        SimpMessageSendingOperations messagingTemplate,
        UserService userService,
        ChatRoomRepository chatRoomRepository,
        ChatRoomService chatRoomService
    ) {
        this.messagingTemplate = messagingTemplate;
        this.chatRoomService = chatRoomService;
    }

    @SendToUser("/queue/messages")
    @KafkaListener(
        topics = "${app.kafka.topic.message-events}",
        groupId = "message-event-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(MessageDTO messageDTO) {
        ChatRoomDTO chatRoomDTO = chatRoomService.findOne(messageDTO.getRoom().getId()).orElseThrow();
        chatRoomDTO.getMembers().forEach(member -> messagingTemplate.convertAndSendToUser(member, "/queue/messages", messageDTO));
        log.debug("Consumed messages event: {}", messageDTO);
    }
}
