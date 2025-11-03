package com.example.chatapp.service.impl;

import com.example.chatapp.domain.ChatRoom;
import com.example.chatapp.repository.ChatRoomRepository;
import com.example.chatapp.service.ChatRoomService;
import com.example.chatapp.service.dto.ChatRoomDTO;
import com.example.chatapp.service.mapper.ChatRoomMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.example.chatapp.domain.ChatRoom}.
 */
@Service
@Transactional
public class ChatRoomServiceImpl implements ChatRoomService {

    private static final Logger LOG = LoggerFactory.getLogger(ChatRoomServiceImpl.class);

    private final ChatRoomRepository chatRoomRepository;

    private final ChatRoomMapper chatRoomMapper;

    public ChatRoomServiceImpl(ChatRoomRepository chatRoomRepository, ChatRoomMapper chatRoomMapper) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMapper = chatRoomMapper;
    }

    @Override
    public ChatRoomDTO save(ChatRoomDTO chatRoomDTO) {
        LOG.debug("Request to save ChatRoom : {}", chatRoomDTO);
        ChatRoom chatRoom = chatRoomMapper.toEntity(chatRoomDTO);
        chatRoom = chatRoomRepository.save(chatRoom);
        return chatRoomMapper.toDto(chatRoom);
    }

    @Override
    public ChatRoomDTO update(ChatRoomDTO chatRoomDTO) {
        LOG.debug("Request to update ChatRoom : {}", chatRoomDTO);
        ChatRoom chatRoom = chatRoomMapper.toEntity(chatRoomDTO);
        chatRoom = chatRoomRepository.save(chatRoom);
        return chatRoomMapper.toDto(chatRoom);
    }

    @Override
    public Optional<ChatRoomDTO> partialUpdate(ChatRoomDTO chatRoomDTO) {
        LOG.debug("Request to partially update ChatRoom : {}", chatRoomDTO);

        return chatRoomRepository
            .findById(chatRoomDTO.getId())
            .map(existingChatRoom -> {
                chatRoomMapper.partialUpdate(existingChatRoom, chatRoomDTO);

                return existingChatRoom;
            })
            .map(chatRoomRepository::save)
            .map(chatRoomMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatRoomDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all ChatRooms");
        return chatRoomRepository.findAll(pageable).map(chatRoomMapper::toDto);
    }

    public Page<ChatRoomDTO> findAllWithEagerRelationships(Pageable pageable) {
        return chatRoomRepository.findAllWithEagerRelationships(pageable).map(chatRoomMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChatRoomDTO> findOne(Long id) {
        LOG.debug("Request to get ChatRoom : {}", id);
        return chatRoomRepository.findOneWithEagerRelationships(id).map(chatRoomMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete ChatRoom : {}", id);
        chatRoomRepository.deleteById(id);
    }
}
