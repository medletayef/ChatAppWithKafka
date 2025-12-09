package com.example.chatapp.service.kafka.room;

import com.example.chatapp.domain.Notification;
import com.example.chatapp.domain.User;
import com.example.chatapp.repository.NotificationRepository;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.service.dto.kafka.RoomEvent;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Service;

@Service
public class RoomEventConsumer {

    private final SimpMessageSendingOperations messagingTemplate;
    private final Logger log = LoggerFactory.getLogger(RoomEventConsumer.class);

    private final NotificationRepository notificationRepository;

    private final UserRepository userRepository;

    public RoomEventConsumer(
        SimpMessagingTemplate messagingTemplate,
        NotificationRepository notificationRepository,
        UserRepository userRepository
    ) {
        this.messagingTemplate = messagingTemplate;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @SendToUser("/queue/room-event")
    @KafkaListener(topics = "${app.kafka.topic.room-events:room-events}", groupId = "room-event-group")
    public void consume(RoomEvent event) {
        log.debug("Consumed room event: {}", event);
        event
            .getRecipients()
            .forEach(recipient -> {
                User user = userRepository.findOneByLogin(recipient).get();
                Optional<Notification> notificationParam = notificationRepository.findByRoom_IdAndUser_Id(event.getRoomId(), user.getId());
                if (
                    !notificationParam.isPresent() || (notificationParam.isPresent() && notificationParam.get().getActive())
                ) messagingTemplate.convertAndSendToUser(recipient, "/queue/room-event", event);
            });
    }
}
