package com.example.chatapp.service.impl;

import com.example.chatapp.domain.Invitation;
import com.example.chatapp.repository.InvitationRepository;
import com.example.chatapp.security.SecurityUtils;
import com.example.chatapp.service.InvitationService;
import com.example.chatapp.service.dto.InvitationDTO;
import com.example.chatapp.service.mapper.InvitationMapper;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InvitationServiceImpl implements InvitationService {

    private final InvitationRepository invitationRepository;
    private final InvitationMapper invitationMapper;

    public InvitationServiceImpl(InvitationRepository invitationRepository, InvitationMapper invitationMapper) {
        this.invitationRepository = invitationRepository;
        this.invitationMapper = invitationMapper;
    }

    @Override
    public InvitationDTO save(InvitationDTO invitationDTO) {
        Invitation invitation = invitationMapper.toEntity(invitationDTO);
        invitation = invitationRepository.save(invitation);
        return invitationMapper.toDto(invitation);
    }

    @Override
    public InvitationDTO update(InvitationDTO invitationDTO) {
        return save(invitationDTO);
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
}
