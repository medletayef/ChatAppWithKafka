package com.example.chatapp.service.kafka.message;

import com.example.chatapp.domain.ChatRoom;
import com.example.chatapp.repository.ChatRoomRepository;
import com.example.chatapp.service.UserService;
import com.example.chatapp.service.dto.ChatRoomDTO;
import com.example.chatapp.service.dto.kafka.MessageDTO;
import com.example.chatapp.service.mapper.ChatRoomMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Service;

@Service
public class MessageEventConsumer {

    private final SimpMessageSendingOperations messagingTemplate;

    private final ChatRoomRepository chatRoomRepository;

    private final ChatRoomMapper chatRoomMapper;

    private final Logger log = LoggerFactory.getLogger(MessageEventConsumer.class);

    public MessageEventConsumer(
        SimpMessageSendingOperations messagingTemplate,
        UserService userService,
        ChatRoomRepository chatRoomRepository,
        ChatRoomMapper chatRoomMapper
    ) {
        this.messagingTemplate = messagingTemplate;
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMapper = chatRoomMapper;
    }

    @SendToUser("/queue/messages")
    @KafkaListener(
        topics = "${app.kafka.topic.message-events}",
        groupId = "message-event-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(MessageDTO messageDTO) {
        ChatRoom room = chatRoomRepository.findById(messageDTO.getRoom().getId()).orElseThrow();
        ChatRoomDTO chatRoomDTO = chatRoomMapper.toDto(room);
        chatRoomDTO.getMembers().forEach(member -> messagingTemplate.convertAndSendToUser(member, "/queue/messages", messageDTO));
        log.debug("Consumed messages event: {}", messageDTO);
    }
}
