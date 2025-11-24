package com.example.chatapp.service;

import com.example.chatapp.service.dto.ChatRoomDTO;
import com.example.chatapp.service.dto.InvitationDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InvitationService {
    InvitationDTO save(InvitationDTO invitationDTO);
    InvitationDTO update(InvitationDTO invitationDTO);
    Optional<InvitationDTO> partialUpdate(InvitationDTO invitationDTO);
    Page<InvitationDTO> findAll(Pageable pageable);
    Page<InvitationDTO> findAllForCurrentUser(Pageable pageable);
    Optional<InvitationDTO> findOne(Long id);
    void delete(Long id);

    Optional<InvitationDTO> findByChatRoomId(Long roomId);

    void inviteMembersToChatroom(ChatRoomDTO chatRoomDTO);
}
