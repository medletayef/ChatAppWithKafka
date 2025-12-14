package com.example.chatapp.service.impl;

import com.example.chatapp.domain.*;
import com.example.chatapp.repository.*;
import com.example.chatapp.security.SecurityUtils;
import com.example.chatapp.service.ChatRoomService;
import com.example.chatapp.service.dto.ChatRoomDTO;
import com.example.chatapp.service.dto.ChatRoomSummaryDto;
import com.example.chatapp.service.dto.NotificationDTO;
import com.example.chatapp.service.dto.kafka.RoomEvent;
import com.example.chatapp.service.kafka.room.RoomEventProducer;
import com.example.chatapp.service.mapper.ChatRoomMapper;
import com.example.chatapp.service.mapper.NotificationMapper;
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

    private final NotificationMapper notificationMapper;

    private final UserRepository userRepository;

    private final ChatRoomRepository chatRoomRepository;

    private final InvitationRepository invitationRepository;

    private final NotificationRepository notificationRepository;

    private final MessageRepository messageRepository;

    private final RoomEventProducer roomEventProducer;

    public ChatRoomServiceImpl(
        UserMapper userMapper,
        NotificationMapper notificationMapper,
        ChatRoomRepository chatRoomRepository,
        ChatRoomMapper chatRoomMapper,
        UserRepository userRepository,
        InvitationRepository invitationRepository,
        NotificationRepository notificationRepository,
        MessageRepository messageRepository,
        RoomEventProducer roomEventProducer
    ) {
        this.userMapper = userMapper;
        this.notificationMapper = notificationMapper;
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMapper = chatRoomMapper;
        this.userRepository = userRepository;
        this.invitationRepository = invitationRepository;
        this.notificationRepository = notificationRepository;
        this.messageRepository = messageRepository;
        this.roomEventProducer = roomEventProducer;
    }

    @Override
    public ChatRoomDTO save(ChatRoomDTO chatRoomDTO) {
        LOG.debug("Request to save ChatRoom : {}", chatRoomDTO);

        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow();
        User currentUser = userRepository.findOneByLogin(currentUserLogin).orElseThrow();
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

        Notification notificationParam = new Notification();
        notificationParam.setRoom(savedChatRoom);
        notificationParam.setUser(currentUser);
        notificationParam.setActive(true);
        notificationRepository.save(notificationParam);

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
            ChatRoomSummaryDto roomSummaryDto = convertChatroomToChatRoomSummaryDTO(chatRoom);
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
    public List<ChatRoomSummaryDto> findByName(String name, int page, int size) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow();
        Pageable pageable = PageRequest.of(page, size);
        List<ChatRoom> chatRoomList = chatRoomRepository.findByNameLike(name, currentUserLogin, pageable).getContent();
        List<ChatRoomSummaryDto> chatRoomSummaryDtos = new ArrayList<>();
        chatRoomList.forEach(chatRoom -> {
            ChatRoomSummaryDto roomSummaryDto = convertChatroomToChatRoomSummaryDTO(chatRoom);
            chatRoomSummaryDtos.add(roomSummaryDto);
        });
        return chatRoomSummaryDtos;
    }

    @Override
    public void muting(Long roomId) {
        String currentLoginUser = SecurityUtils.getCurrentUserLogin().orElseThrow();
        User currentUser = userRepository.findOneByLogin(currentLoginUser).orElseThrow();
        Notification notificationParam = notificationRepository.findByRoom_IdAndUser_Id(roomId, currentUser.getId()).orElseThrow();
        notificationParam.setActive(!notificationParam.getActive());
        notificationRepository.save(notificationParam);
    }

    @Override
    public NotificationDTO getRoomNotificationParam(Long roomId) {
        String currentLoginUser = SecurityUtils.getCurrentUserLogin().orElseThrow();
        User currentUser = userRepository.findOneByLogin(currentLoginUser).orElseThrow();
        Notification notificationParam = notificationRepository.findByRoom_IdAndUser_Id(roomId, currentUser.getId()).orElseThrow();
        return notificationMapper.toDto(notificationParam);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChatRoomSummaryDto> findOne(Long id) {
        LOG.debug("Request to get ChatRoom : {}", id);
        Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findById(id);
        if (chatRoomOptional.isPresent()) {
            ChatRoomSummaryDto roomSummaryDto = convertChatroomToChatRoomSummaryDTO(chatRoomOptional.orElseThrow());
            return Optional.of(roomSummaryDto);
        }
        return Optional.empty();
    }

    @Override
    public ChatRoomDTO leaveRoom(Long roomId) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow();
        User currentUser = userRepository.findOneByLogin(currentUserLogin).orElseThrow();
        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
        ChatRoomDTO roomDTO = chatRoomMapper.toDto(room);
        if (!currentUserLogin.equals(room.getCreatedBy())) {
            if (!roomDTO.getMembers().contains(currentUserLogin)) throw new BadRequestAlertException(
                "this account not member of the room",
                "",
                "notMember"
            );
            roomDTO.setMembers(
                roomDTO.getMembers().stream().filter(member -> !member.equals(currentUserLogin)).collect(Collectors.toSet())
            );
            Optional<Invitation> invitationOptional = invitationRepository.findByChatRoomIdAndUserId(room.getId(), currentUser.getId());
            if (invitationOptional.isPresent()) invitationRepository.deleteById(invitationOptional.orElseThrow().getId());
            Notification notification = notificationRepository.findByRoom_IdAndUser_Id(room.getId(), currentUser.getId()).orElseThrow();
            notificationRepository.deleteById(notification.getId());
            room = chatRoomMapper.toEntity(roomDTO);
            chatRoomRepository.save(room);
            RoomEvent roomEvent = new RoomEvent();
            roomEvent.setRoomId(roomDTO.getId());
            roomEvent.setRoomName(roomDTO.getName());
            roomEvent.setSender(currentUser.getFirstName() + " " + currentUser.getLastName());
            roomEvent.setRecipients(roomDTO.getMembers());
            roomEvent.setType(RoomEvent.RoomEventType.ROOM_LEFT);
            roomEventProducer.publish(roomEvent);
        }
        return roomDTO;
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete ChatRoom : {}", id);
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow();
        User currentUser = userRepository.findOneByLogin(currentUserLogin).orElseThrow();
        ChatRoom room = chatRoomRepository.findById(id).orElseThrow();
        if (currentUserLogin.equals(room.getCreatedBy())) {
            invitationRepository.deleteAllByChatRoom_Id(room.getId());
            notificationRepository.deleteAllByRoom_Id(room.getId());
            messageRepository.deleteAllByRoom_Id(room.getId());
            chatRoomRepository.deleteById(id);
            RoomEvent roomEvent = new RoomEvent();
            roomEvent.setRoomId(room.getId());
            roomEvent.setRoomName(room.getName());
            roomEvent.setSender(currentUser.getFirstName() + " " + currentUser.getLastName());
            roomEvent.setType(RoomEvent.RoomEventType.ROOM_DELETED);
            roomEvent.setRecipients(
                room.getMembers().stream().map(User::getLogin).filter(login -> !login.equals(currentUserLogin)).collect(Collectors.toSet())
            );
            roomEventProducer.publish(roomEvent);
        }
    }

    public ChatRoomSummaryDto convertChatroomToChatRoomSummaryDTO(ChatRoom chatRoom) {
        ChatRoomSummaryDto roomSummaryDto = new ChatRoomSummaryDto();
        roomSummaryDto.setId(chatRoom.getId());
        roomSummaryDto.setName(chatRoom.getName());
        roomSummaryDto.setCreatedBy(chatRoom.getCreatedBy());
        roomSummaryDto.setMembers(chatRoom.getMembers().stream().map(userMapper::userToUserDTO).collect(Collectors.toSet()));
        List<Message> messageList = chatRoomRepository.findLastMessageOfRoom(chatRoom.getId(), PageRequest.of(0, 1));
        if (!messageList.isEmpty()) {
            User sender = userRepository.findOneByLogin(chatRoom.getCreatedBy()).orElseThrow();
            roomSummaryDto.setSender(sender.getFirstName() + " " + sender.getLastName());
            roomSummaryDto.setLastMessage(messageList.get(0).getContent());
            roomSummaryDto.setLastMsgSentAt(messageList.get(0).getSentAt());
            roomSummaryDto.setSenderImageUrl(sender.getImageUrl());
        }
        return roomSummaryDto;
    }
}
