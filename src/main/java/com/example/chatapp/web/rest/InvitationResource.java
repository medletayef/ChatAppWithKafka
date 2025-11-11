package com.example.chatapp.web.rest;

import com.example.chatapp.service.InvitationService;
import com.example.chatapp.service.dto.InvitationDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
