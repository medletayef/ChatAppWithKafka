package com.example.chatapp.service.impl;

import com.example.chatapp.domain.ChatRoom;
import com.example.chatapp.domain.Invitation;
import com.example.chatapp.domain.User;
import com.example.chatapp.domain.enumeration.InvitationStatus;
import com.example.chatapp.repository.ChatRoomRepository;
import com.example.chatapp.repository.InvitationRepository;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.security.SecurityUtils;
import com.example.chatapp.service.ChatRoomService;
import com.example.chatapp.service.dto.ChatRoomDTO;
import com.example.chatapp.service.dto.kafka.RoomEvent;
import com.example.chatapp.service.kafka.room.RoomEventProducer;
import com.example.chatapp.service.mapper.ChatRoomMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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

    private final UserRepository userRepository;

    private final InvitationRepository invitationRepository;

    private final RoomEventProducer roomEventProducer;

    public ChatRoomServiceImpl(
        ChatRoomRepository chatRoomRepository,
        ChatRoomMapper chatRoomMapper,
        UserRepository userRepository,
        InvitationRepository invitationRepository,
        RoomEventProducer roomEventProducer
    ) {
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
        User creator = userRepository.findOneByLogin(currentUserLogin).orElseThrow();

        ChatRoom chatRoom = chatRoomMapper.toEntity(chatRoomDTO);

        // --- Fetch members by login ---
        Set<User> members = new HashSet<>();
        if (chatRoomDTO.getMembers() != null && !chatRoomDTO.getMembers().isEmpty()) {
            members = chatRoomDTO
                .getMembers()
                .stream()
                .map(userRepository::findOneByLogin)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        }

        // --- Ensure current user is in the room ---
        members.add(creator);
        chatRoom.setMembers(members);

        // Save the chat room first (so it gets an ID)
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        // Create invitations for all members except the creator
        for (User member : members) {
            if (!member.getId().equals(creator.getId())) {
                Invitation invitation = new Invitation();
                invitation.setChatRoom(savedChatRoom);
                invitation.setUser(member);
                invitation.setStatus(InvitationStatus.PENDING);
                invitationRepository.save(invitation);
                LOG.debug("Created invitation for member {} to room {}", member.getLogin(), savedChatRoom.getName());
            }
        }

        Set<String> receipients = members
            .stream()
            .filter(user -> user.getId() != creator.getId())
            .map(User::getLogin)
            .collect(Collectors.toSet());
        RoomEvent roomEvent = new RoomEvent();
        roomEvent.setRoomId(savedChatRoom.getId());
        roomEvent.setRoomName(savedChatRoom.getName());
        roomEvent.setSender(creator.getFirstName() + " " + creator.getLastName());
        roomEvent.setType(RoomEvent.RoomEventType.INVITATION_SENT);
        roomEvent.setRecipients(receipients);
        roomEventProducer.publish(roomEvent);

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
    public List<ChatRoomDTO> findAllRelatedRooms(String memberLogin) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow();
        Long memberId = userRepository.findOneByLogin(memberLogin).get().getId();
        List<ChatRoomDTO> chatRooms = chatRoomRepository
            .findAllRelatedRooms(memberId, memberLogin, currentUserLogin)
            .stream()
            .map(chatRoomMapper::toDto)
            .collect(Collectors.toList());
        return chatRooms;
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
