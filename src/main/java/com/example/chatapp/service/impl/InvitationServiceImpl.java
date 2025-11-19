package com.example.chatapp.service.impl;

import static org.hibernate.id.IdentifierGenerator.ENTITY_NAME;

import com.example.chatapp.domain.ChatRoom;
import com.example.chatapp.domain.Invitation;
import com.example.chatapp.domain.User;
import com.example.chatapp.repository.ChatRoomRepository;
import com.example.chatapp.repository.InvitationRepository;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.security.SecurityUtils;
import com.example.chatapp.service.InvitationService;
import com.example.chatapp.service.dto.ChatRoomDTO;
import com.example.chatapp.service.dto.InvitationDTO;
import com.example.chatapp.service.dto.kafka.RoomEvent;
import com.example.chatapp.service.mapper.ChatRoomMapper;
import com.example.chatapp.service.mapper.InvitationMapper;
import com.example.chatapp.web.rest.errors.BadRequestAlertException;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InvitationServiceImpl implements InvitationService {

    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;

    private final ChatRoomRepository chatRoomRepository;
    private final InvitationMapper invitationMapper;

    private final ChatRoomMapper chatRoomMapper;

    private final SimpMessageSendingOperations messagingTemplate;

    public InvitationServiceImpl(
        UserRepository userRepository,
        InvitationRepository invitationRepository,
        ChatRoomRepository chatRoomRepository,
        InvitationMapper invitationMapper,
        ChatRoomMapper chatRoomMapper,
        SimpMessageSendingOperations messagingTemplate
    ) {
        this.userRepository = userRepository;
        this.invitationRepository = invitationRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.invitationMapper = invitationMapper;
        this.chatRoomMapper = chatRoomMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public InvitationDTO save(InvitationDTO invitationDTO) {
        Invitation invitation = invitationMapper.toEntity(invitationDTO);
        invitation = invitationRepository.save(invitation);
        return invitationMapper.toDto(invitation);
    }

    @Override
    @SendToUser("/queue/room-event")
    public InvitationDTO update(InvitationDTO invitationDTO) {
        if (invitationDTO.getId() == null) throw new BadRequestAlertException("invitation id ", ENTITY_NAME, "not provided");
        if (!invitationRepository.findById(invitationDTO.getId()).isPresent()) throw new BadRequestAlertException(
            "Invitation id not found",
            ENTITY_NAME,
            "idnotfound"
        );
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().get();
        User currentUser = userRepository.findOneByLogin(currentUserLogin).get();
        ChatRoom room = chatRoomRepository.findById(invitationDTO.getChatRoom().getId()).get();
        ChatRoomDTO chatRoomDTO = chatRoomMapper.toDto(room);
        if (!chatRoomDTO.getMembers().contains(currentUserLogin)) throw new BadRequestAlertException(
            "current user doesn't belong to the room invited in ",
            "Invitation",
            ""
        );
        InvitationDTO dto = save(invitationDTO);
        if (invitationDTO.getStatus().toString().equals("ACCEPTED")) {
            RoomEvent roomEvent = new RoomEvent();
            roomEvent.setRoomName(chatRoomDTO.getName());
            roomEvent.setType(RoomEvent.RoomEventType.ROOM_JOINED);
            roomEvent.setReceiver(currentUser.getFirstName() + " " + currentUser.getLastName());
            chatRoomDTO
                .getMembers()
                .stream()
                .filter(member -> member != currentUserLogin)
                .forEach(login -> messagingTemplate.convertAndSendToUser(login, "/queue/room-event", roomEvent));
        }
        return dto;
    }

    @Override
    public Optional<InvitationDTO> partialUpdate(InvitationDTO invitationDTO) {
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
}
