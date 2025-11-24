package com.example.chatapp.web.rest;

import com.example.chatapp.service.InvitationService;
import com.example.chatapp.service.dto.ChatRoomDTO;
import com.example.chatapp.service.dto.InvitationDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

@RestController
@RequestMapping("/api/invitations")
public class InvitationResource {

    private static final String ENTITY_NAME = "invitation";

    private final InvitationService invitationService;

    public InvitationResource(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    /**
     * GET  /invitations : get all invitations for current user (paginated)
     */
    @GetMapping("")
    public ResponseEntity<List<InvitationDTO>> getCurrentUserInvitations(Pageable pageable) {
        Page<InvitationDTO> page = invitationService.findAllForCurrentUser(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/byRoomId")
    public ResponseEntity<InvitationDTO> getInvitationByRoomId(@RequestParam("roomId") Long roomId) {
        Optional<InvitationDTO> invitationDTO = invitationService.findByChatRoomId(roomId);
        return ResponseEntity.ok(invitationDTO.get());
    }

    @PutMapping("")
    public ResponseEntity<InvitationDTO> updateInvitation(@RequestBody InvitationDTO invitationDTO) {
        InvitationDTO dto = invitationService.update(invitationDTO);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/invite-members")
    public ResponseEntity<Void> inviteMembers(@RequestBody ChatRoomDTO chatRoomDTO) {
        invitationService.inviteMembersToChatroom(chatRoomDTO);
        return ResponseEntity.ok().build();
    }
}
