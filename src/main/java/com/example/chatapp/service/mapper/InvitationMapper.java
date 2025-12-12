package com.example.chatapp.service.mapper;

import com.example.chatapp.domain.Invitation;
import com.example.chatapp.service.dto.InvitationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { ChatRoomMapper.class, UserMapper.class })
public interface InvitationMapper extends EntityMapper<InvitationDTO, Invitation> {
    @Mapping(source = "chatRoom", target = "chatRoom")
    @Mapping(source = "user", target = "user")
    @Mapping(source = "createdDate", target = "createdDate")
    InvitationDTO toDto(Invitation invitation);

    @Mapping(source = "chatRoom", target = "chatRoom")
    @Mapping(source = "user", target = "user")
    Invitation toEntity(InvitationDTO invitationDTO);

    default Invitation fromId(Long id) {
        if (id == null) {
            return null;
        }
        Invitation invitation = new Invitation();
        invitation.setId(id);
        return invitation;
    }
}
