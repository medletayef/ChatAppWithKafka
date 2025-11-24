package com.example.chatapp.service.impl;

import com.example.chatapp.domain.ChatRoom;
import com.example.chatapp.domain.Message;
import com.example.chatapp.domain.User;
import com.example.chatapp.repository.ChatRoomRepository;
import com.example.chatapp.repository.MessageRepository;
import com.example.chatapp.security.SecurityUtils;
import com.example.chatapp.service.MessageService;
import com.example.chatapp.service.UserService;
import com.example.chatapp.service.dto.ChatRoomDTO;
import com.example.chatapp.service.dto.UserDTO;
import com.example.chatapp.service.dto.kafka.MessageDTO;
import com.example.chatapp.service.kafka.message.MessageEventProducer;
import com.example.chatapp.service.mapper.ChatRoomMapper;
import com.example.chatapp.service.mapper.MessageMapper;
import com.example.chatapp.web.rest.errors.BadRequestAlertException;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.example.chatapp.domain.Message}.
 */
@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final MessageRepository messageRepository;

    private final MessageMapper messageMapper;

    private final ChatRoomRepository chatRoomRepository;

    private final ChatRoomMapper chatRoomMapper;

    private final UserService userService;

    private final MessageEventProducer messageEventProducer;

    public MessageServiceImpl(
        MessageRepository messageRepository,
        MessageMapper messageMapper,
        ChatRoomRepository chatRoomRepository,
        ChatRoomMapper chatRoomMapper,
        UserService userService,
        MessageEventProducer messageEventProducer
    ) {
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMapper = chatRoomMapper;
        this.userService = userService;
        this.messageEventProducer = messageEventProducer;
    }

    @Override
    public MessageDTO save(MessageDTO messageDTO) {
        LOG.debug("Request to save Message : {}", messageDTO);
        ChatRoom chatRoom = chatRoomRepository.findById(messageDTO.getRoom().getId()).orElseThrow();
        ChatRoomDTO chatRoomDTO = chatRoomMapper.toDto(chatRoom);
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().get();
        User currentUser = userService.getUserWithAuthoritiesByLogin(currentUserLogin).orElseThrow();
        if (!chatRoomDTO.getMembers().contains(currentUserLogin)) throw new BadRequestAlertException(
            "user must be member to send message this room",
            "",
            "userMessageNotMember"
        );
        Message message = messageMapper.toEntity(messageDTO);
        message = messageRepository.save(message);
        chatRoom.setLastMsgSentAt(Instant.now());
        chatRoomRepository.save(chatRoom);
        MessageDTO dto = new MessageDTO();
        UserDTO sender = new UserDTO();
        dto.setId(message.getId());
        dto.setContent(messageMapper.toDto(message).getContent());
        sender.setId(messageMapper.toDto(message).getSender().getId());
        sender.setLogin(currentUserLogin);
        sender.setFullName(currentUser.getFirstName() + " " + currentUser.getLastName());
        sender.setImageUrl(currentUser.getImageUrl());
        dto.setSender(sender);
        dto.setRoom(chatRoomDTO);
        dto.setSentAt(Instant.now());
        messageEventProducer.publish(dto);
        return dto;
    }

    @Override
    public MessageDTO update(MessageDTO messageDTO) {
        LOG.debug("Request to update Message : {}", messageDTO);
        Message message = messageMapper.toEntity(messageDTO);
        message = messageRepository.save(message);
        return messageMapper.toDto(message);
    }

    @Override
    public Optional<MessageDTO> partialUpdate(MessageDTO messageDTO) {
        LOG.debug("Request to partially update Message : {}", messageDTO);

        return messageRepository
            .findById(messageDTO.getId())
            .map(existingMessage -> {
                messageMapper.partialUpdate(existingMessage, messageDTO);

                return existingMessage;
            })
            .map(messageRepository::save)
            .map(messageMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Messages");
        return messageRepository.findAll(pageable).map(messageMapper::toDto);
    }

    public Page<MessageDTO> findAllWithEagerRelationships(Pageable pageable) {
        return messageRepository.findAllWithEagerRelationships(pageable).map(messageMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MessageDTO> findOne(Long id) {
        LOG.debug("Request to get Message : {}", id);
        return messageRepository.findOneWithEagerRelationships(id).map(messageMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Message : {}", id);
        messageRepository.deleteById(id);
    }

    @Override
    public Page<MessageDTO> getMessagesByRoom(Long roomId, Pageable pageable) {
        return messageRepository.findByRoom_IdOrderBySentAtDesc(roomId, pageable).map(messageMapper::toDto);
    }
}
