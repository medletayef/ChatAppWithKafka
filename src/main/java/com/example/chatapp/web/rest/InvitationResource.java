package com.example.chatapp.web.rest;

import com.example.chatapp.service.InvitationService;
import com.example.chatapp.service.dto.ChatRoomDTO;
import com.example.chatapp.service.dto.InvitationDTO;
import com.example.chatapp.service.impl.ChatRoomServiceImpl;
import com.example.chatapp.web.rest.errors.BadRequestAlertException;
import jakarta.validation.constraints.NotNull;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api/invitations")
public class InvitationResource {

    private static final Logger LOG = LoggerFactory.getLogger(InvitationResource.class);
    private static final String ENTITY_NAME = "invitation";

    private final InvitationService invitationService;

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

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

    @GetMapping("/get-invitations")
    public ResponseEntity<Page<InvitationDTO>> getInvitationsByReceiverUser(
        @RequestParam("page") int page,
        @RequestParam("size") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InvitationDTO> invitationDTOList = invitationService.findByReceiverUser(pageable);
        return ResponseEntity.ok(invitationDTOList);
    }

    @PutMapping("")
    public ResponseEntity<InvitationDTO> updateInvitation(@RequestBody InvitationDTO invitationDTO) {
        InvitationDTO dto = invitationService.update(invitationDTO);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<InvitationDTO> partialUpdateInvitation(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody InvitationDTO invitationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Invitation partially : {}, {}", id, invitationDTO);
        if (invitationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, invitationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (invitationService.findOne(id).isEmpty()) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<InvitationDTO> result = invitationService.partialUpdate(invitationDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, invitationDTO.getId().toString())
        );
    }

    @PostMapping("/invite-members")
    public ResponseEntity<Void> inviteMembers(@RequestBody ChatRoomDTO chatRoomDTO) {
        invitationService.inviteMembersToChatroom(chatRoomDTO);
        return ResponseEntity.ok().build();
    }
}
