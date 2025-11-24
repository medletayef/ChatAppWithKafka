package com.example.chatapp.service.impl;

import com.example.chatapp.domain.ChatRoom;
import com.example.chatapp.domain.Message;
import com.example.chatapp.domain.User;
import com.example.chatapp.repository.ChatRoomRepository;
import com.example.chatapp.repository.InvitationRepository;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.security.SecurityUtils;
import com.example.chatapp.service.ChatRoomService;
import com.example.chatapp.service.dto.ChatRoomDTO;
import com.example.chatapp.service.dto.ChatRoomSummaryDto;
import com.example.chatapp.service.kafka.room.RoomEventProducer;
import com.example.chatapp.service.mapper.ChatRoomMapper;
import com.example.chatapp.service.mapper.UserMapper;
import com.example.chatapp.web.rest.errors.BadRequestAlertException;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    private final UserMapper userMapper;
    private final ChatRoomMapper chatRoomMapper;

    private final UserRepository userRepository;

    private final ChatRoomRepository chatRoomRepository;

    private final InvitationRepository invitationRepository;

    private final RoomEventProducer roomEventProducer;

    public ChatRoomServiceImpl(
        UserMapper userMapper,
        ChatRoomRepository chatRoomRepository,
        ChatRoomMapper chatRoomMapper,
        UserRepository userRepository,
        InvitationRepository invitationRepository,
        RoomEventProducer roomEventProducer
    ) {
        this.userMapper = userMapper;
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMapper = chatRoomMapper;
        this.userRepository = userRepository;
        this.invitationRepository = invitationRepository;
        this.roomEventProducer = roomEventProducer;
    }

    @Override
    public ChatRoomDTO save(ChatRoomDTO chatRoomDTO) {
        LOG.debug("Request to save ChatRoom : {}", chatRoomDTO);

        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow();

        boolean validMembers =
            (chatRoomDTO.getMembers().size() == 0) ||
            (chatRoomDTO.getMembers().size() == 1 && chatRoomDTO.getMembers().contains(currentUserLogin));

        if (!validMembers) throw new BadRequestAlertException("initially room created must contain at zero member or the creator", "", "");

        Set<String> set = new HashSet<>();
        if (chatRoomDTO.getMembers().isEmpty()) {
            set.add(currentUserLogin);
            chatRoomDTO.setMembers(set);
        }
        ChatRoom chatRoom = chatRoomMapper.toEntity(chatRoomDTO);

        // Save the chat room first (so it gets an ID)
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        return chatRoomMapper.toDto(savedChatRoom);
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
    public List<ChatRoomSummaryDto> findRelatedRooms(String memberLogin, int page, int size) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow();

        Pageable pageable = PageRequest.of(page, size);

        List<ChatRoomSummaryDto> chatRoomSummaryDtos = new ArrayList<>();
        List<ChatRoom> chatRooms = new ArrayList<>();
        if (memberLogin == null || (memberLogin != null && memberLogin.isEmpty())) {
            chatRooms = chatRoomRepository.findRecentRelatedRooms(currentUserLogin, pageable).getContent();
        } else {
            chatRooms = chatRoomRepository.findRecentRelatedRoomsToMember(memberLogin, currentUserLogin, pageable).getContent();
        }
        chatRooms.forEach(chatRoom -> {
            ChatRoomSummaryDto roomSummaryDto = new ChatRoomSummaryDto();
            roomSummaryDto.setId(chatRoom.getId());
            roomSummaryDto.setName(chatRoom.getName());
            roomSummaryDto.setCreatedBy(chatRoom.getCreatedBy());
            roomSummaryDto.setMembers(chatRoom.getMembers().stream().map(userMapper::userToUserDTO).collect(Collectors.toSet()));
            List<Message> messageList = chatRoomRepository.findLastMessageOfRoom(chatRoom.getId(), PageRequest.of(0, 1));
            if (!messageList.isEmpty()) {
                User sender = userRepository.findOneByLogin(chatRoom.getCreatedBy()).get();
                roomSummaryDto.setSender(sender.getFirstName() + " " + sender.getLastName());
                roomSummaryDto.setLastMessage(messageList.get(0).getContent());
                roomSummaryDto.setLastMsgSentAt(messageList.get(0).getSentAt());
                roomSummaryDto.setSenderImageUrl(sender.getImageUrl());
            }
            chatRoomSummaryDtos.add(roomSummaryDto);
        });

        return chatRoomSummaryDtos;
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
