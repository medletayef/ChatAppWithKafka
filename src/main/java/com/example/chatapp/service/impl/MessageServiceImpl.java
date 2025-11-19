package com.example.chatapp.service.impl;

import com.example.chatapp.domain.Message;
import com.example.chatapp.repository.MessageRepository;
import com.example.chatapp.service.MessageService;
import com.example.chatapp.service.dto.MessageDTO;
import com.example.chatapp.service.mapper.MessageMapper;
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

    public MessageServiceImpl(MessageRepository messageRepository, MessageMapper messageMapper) {
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
    }

    @Override
    public MessageDTO save(MessageDTO messageDTO) {
        LOG.debug("Request to save Message : {}", messageDTO);
        Message message = messageMapper.toEntity(messageDTO);
        message = messageRepository.save(message);
        return messageMapper.toDto(message);
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
