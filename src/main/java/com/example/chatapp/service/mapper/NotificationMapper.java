package com.example.chatapp.service.mapper;

import com.example.chatapp.domain.Notification;
import com.example.chatapp.service.dto.NotificationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { ChatRoomMapper.class, UserMapper.class })
public interface NotificationMapper extends EntityMapper<NotificationDTO, Notification> {
    @Mapping(source = "room", target = "room")
    @Mapping(source = "user", target = "user")
    NotificationDTO toDto(Notification Notification);

    @Mapping(source = "room", target = "room")
    @Mapping(source = "user", target = "user")
    Notification toEntity(NotificationDTO NotificationDTO);

    default Notification fromId(Long id) {
        if (id == null) {
            return null;
        }
        Notification notification = new Notification();
        notification.setId(id);
        return notification;
    }
}
