package com.example.chatapp.service.kafka.message;

import com.example.chatapp.service.dto.kafka.MessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final Logger log = LoggerFactory.getLogger(MessageEventProducer.class);

    @Value("${app.kafka.topic.message-events}")
    private String topic;

    public MessageEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(MessageDTO messageDTO) {
        this.kafkaTemplate.send(topic, messageDTO.getRoom().getId().toString(), messageDTO);
        log.info("📤 Sent MessageEvent: type={}, topic={}", "message", topic + messageDTO.getRoom().getId());
    }
}
