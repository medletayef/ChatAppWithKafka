package com.example.chatapp.service.impl;

import static org.hibernate.id.IdentifierGenerator.ENTITY_NAME;

import com.example.chatapp.domain.ChatRoom;
import com.example.chatapp.domain.Invitation;
import com.example.chatapp.domain.Notification;
import com.example.chatapp.domain.User;
import com.example.chatapp.domain.enumeration.InvitationStatus;
import com.example.chatapp.repository.ChatRoomRepository;
import com.example.chatapp.repository.InvitationRepository;
import com.example.chatapp.repository.NotificationRepository;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.security.SecurityUtils;
import com.example.chatapp.service.ChatRoomService;
import com.example.chatapp.service.InvitationService;
import com.example.chatapp.service.dto.ChatRoomDTO;
import com.example.chatapp.service.dto.InvitationDTO;
import com.example.chatapp.service.dto.UserDTO;
import com.example.chatapp.service.dto.kafka.RoomEvent;
import com.example.chatapp.service.kafka.room.RoomEventProducer;
import com.example.chatapp.service.mapper.ChatRoomMapper;
import com.example.chatapp.service.mapper.InvitationMapper;
import com.example.chatapp.service.mapper.UserMapper;
import com.example.chatapp.web.rest.errors.BadRequestAlertException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.aspectj.weaver.ast.Not;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InvitationServiceImpl implements InvitationService {

    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;

    private final ChatRoomRepository chatRoomRepository;

    private final NotificationRepository notificationRepository;

    private final ChatRoomService chatRoomService;
    private final InvitationMapper invitationMapper;

    private final ChatRoomMapper chatRoomMapper;

    private final UserMapper userMapper;

    private final RoomEventProducer roomEventProducer;

    public InvitationServiceImpl(
        UserRepository userRepository,
        InvitationRepository invitationRepository,
        ChatRoomRepository chatRoomRepository,
        NotificationRepository notificationRepository,
        ChatRoomService chatRoomService,
        InvitationMapper invitationMapper,
        ChatRoomMapper chatRoomMapper,
        UserMapper userMapper,
        RoomEventProducer roomEventProducer
    ) {
        this.userRepository = userRepository;
        this.invitationRepository = invitationRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.notificationRepository = notificationRepository;
        this.chatRoomService = chatRoomService;
        this.invitationMapper = invitationMapper;
        this.chatRoomMapper = chatRoomMapper;
        this.userMapper = userMapper;
        this.roomEventProducer = roomEventProducer;
    }

    @Override
    public InvitationDTO save(InvitationDTO invitationDTO) {
        Invitation invitation = invitationMapper.toEntity(invitationDTO);
        invitation = invitationRepository.save(invitation);
        return invitationMapper.toDto(invitation);
    }

    @Override
    public InvitationDTO update(InvitationDTO invitationDTO) {
        sendRoomEventWhenInvitationStatusUpdated(invitationDTO);
        InvitationDTO dto = save(invitationDTO);
        return dto;
    }

    @Override
    public Optional<InvitationDTO> partialUpdate(InvitationDTO invitationDTO) {
        this.sendRoomEventWhenInvitationStatusUpdated(invitationDTO);
        return invitationRepository
            .findById(invitationDTO.getId())
            .map(existing -> {
                if (invitationDTO.getStatus() != null) existing.setStatus(invitationDTO.getStatus());
                if (invitationDTO.getChatRoom() != null) existing.setChatRoom(invitationMapper.toEntity(invitationDTO).getChatRoom());
                if (invitationDTO.getUser() != null) existing.setUser(invitationMapper.toEntity(invitationDTO).getUser());
                return existing;
            })
            .map(invitationRepository::save)
            .map(invitationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvitationDTO> findAll(Pageable pageable) {
        return invitationRepository.findAll(pageable).map(invitationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvitationDTO> findAllForCurrentUser(Pageable pageable) {
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElseThrow();
        return invitationRepository.findByUserLogin(currentLogin, pageable).map(invitationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InvitationDTO> findOne(Long id) {
        return invitationRepository.findById(id).map(invitationMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        invitationRepository.deleteById(id);
    }

    @Override
    public Optional<InvitationDTO> findByChatRoomId(Long roomId) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().get();
        User currentUser = userRepository.findOneByLogin(currentUserLogin).get();
        return invitationRepository.findByChatRoomIdAndUserId(roomId, currentUser.getId()).map(invitationMapper::toDto);
    }

    @Override
    public void inviteMembersToChatroom(ChatRoomDTO chatRoomDTO) {
        if (!chatRoomRepository.findById(chatRoomDTO.getId()).isPresent()) throw new BadRequestAlertException("Room not found", "Room", "");
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().get();
        User currentUser = userRepository.findOneByLogin(currentUserLogin).get();
        ChatRoom room = chatRoomRepository.findById(chatRoomDTO.getId()).get();
        ChatRoomDTO roomDTO = chatRoomMapper.toDto(room);
        if (!roomDTO.getCreatedBy().equals(currentUserLogin)) throw new BadRequestAlertException(
            "you must be the creator of room to invite members",
            ENTITY_NAME,
            ""
        );

        Set<String> set = chatRoomDTO
            .getMembers()
            .stream()
            .distinct()
            .filter(login -> userRepository.findOneByLogin(login).isPresent() && !roomDTO.getMembers().contains(login))
            .collect(Collectors.toSet());

        RoomEvent roomEvent = new RoomEvent();
        roomEvent.setRoomId(chatRoomDTO.getId());
        roomEvent.setSender(currentUser.getFirstName() + " " + currentUser.getLastName());
        roomEvent.setType(RoomEvent.RoomEventType.INVITATION_SENT);
        roomEvent.setRecipients(set);
        roomEvent.setRoomName(roomDTO.getName());
        set.forEach(element -> {
            InvitationDTO invitationDTO = new InvitationDTO();
            invitationDTO.setChatRoom(chatRoomDTO);
            invitationDTO.setStatus(InvitationStatus.PENDING);
            User user = userRepository.findOneByLogin(element).get();
            UserDTO recipient = userMapper.userToUserDTO(user);
            invitationDTO.setUser(recipient);
            Optional<Invitation> invitationOptional = invitationRepository.findByChatRoomIdAndUserId(roomDTO.getId(), recipient.getId());
            if (invitationOptional.isPresent()) invitationRepository.deleteById(invitationOptional.get().getId());
            save(invitationDTO);
            Optional<Notification> optionalNotification = notificationRepository.findByRoom_IdAndUser_Id(room.getId(), user.getId());
            if (!optionalNotification.isPresent()) {
                Notification notification = new Notification();
                notification.setUser(user);
                notification.setRoom(room);
                notification.setActive(true);
                notificationRepository.save(notification);
            }
        });
        roomEventProducer.publish(roomEvent);
    }

    @Override
    public Page<InvitationDTO> findByReceiverUser(Pageable pageable) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().get();
        User currentUser = userRepository.findOneByLogin(currentUserLogin).get();
        return invitationRepository.findByUser_Id(currentUser.getId(), pageable).map(invitationMapper::toDto);
    }

    void sendRoomEventWhenInvitationStatusUpdated(InvitationDTO invitationDTO) {
        if (invitationDTO.getStatus() != null && invitationDTO.getStatus().equals("PENDING")) throw new BadRequestAlertException(
            "invalid operation can't set invitation status to pending",
            ENTITY_NAME,
            "invalidStatus"
        );
        if (invitationDTO.getId() == null) throw new BadRequestAlertException("invitation id not provided", ENTITY_NAME, "");
        if (!invitationRepository.findById(invitationDTO.getId()).isPresent()) throw new BadRequestAlertException(
            "Invitation id not found",
            ENTITY_NAME,
            "idnotFound"
        );
        Invitation invitation = invitationRepository.findById(invitationDTO.getId()).orElseThrow();
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().get();
        User currentUser = userRepository.findOneByLogin(currentUserLogin).get();
        boolean invalid = !(invitation.getUser().getId() == currentUser.getId());
        if (invalid) throw new BadRequestAlertException("invalid invitation", ENTITY_NAME, "");
        ChatRoom room = chatRoomRepository.findById(invitation.getChatRoom().getId()).get();
        ChatRoomDTO chatRoomDTO = chatRoomMapper.toDto(room);
        User roomCreator = userRepository.findOneByLogin(room.getCreatedBy()).get();

        Optional<Notification> optionalNotification = notificationRepository.findByRoom_IdAndUser_Id(room.getId(), currentUser.getId());
        Notification notification = new Notification();
        if (optionalNotification.isPresent()) {
            notification = optionalNotification.get();
        } else {
            notification.setActive(true);
            notification.setRoom(room);
            notification.setUser(currentUser);
            notificationRepository.save(notification);
        }

        RoomEvent roomEvent = new RoomEvent();
        roomEvent.setRoomId(room.getId());
        roomEvent.setRoomName(chatRoomDTO.getName());
        roomEvent.setSender(currentUser.getFirstName() + " " + currentUser.getLastName());
        roomEvent.setReceiver(roomCreator.getFirstName() + " " + roomCreator.getLastName());
        if (invitationDTO.getStatus().toString().equals("ACCEPTED")) {
            chatRoomDTO.getMembers().add(currentUserLogin);
            chatRoomRepository.save(chatRoomMapper.toEntity(chatRoomDTO));
            roomEvent.setType(RoomEvent.RoomEventType.ROOM_JOINED);
            roomEvent.setRecipients(
                room.getMembers().stream().map(User::getLogin).filter(login -> !login.equals(currentUserLogin)).collect(Collectors.toSet())
            );
        } else if (invitationDTO.getStatus().toString().equals("REJECTED")) {
            roomEvent.setType(RoomEvent.RoomEventType.ROOM_REJECTED);
            roomEvent.setRecipients(Set.of(invitation.getCreatedBy()));
        }
        roomEventProducer.publish(roomEvent);
    }
}
