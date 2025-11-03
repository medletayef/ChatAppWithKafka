package com.example.chatapp.service.mapper;

import com.example.chatapp.domain.ChatRoom;
import com.example.chatapp.domain.User;
import com.example.chatapp.service.dto.ChatRoomDTO;
import com.example.chatapp.service.dto.UserDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ChatRoom} and its DTO {@link ChatRoomDTO}.
 */
@Mapper(componentModel = "spring")
public interface ChatRoomMapper extends EntityMapper<ChatRoomDTO, ChatRoom> {
    @Mapping(target = "members", source = "members", qualifiedByName = "userLoginSet")
    ChatRoomDTO toDto(ChatRoom s);

    @Mapping(target = "removeMembers", ignore = true)
    ChatRoom toEntity(ChatRoomDTO chatRoomDTO);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("userLoginSet")
    default Set<UserDTO> toDtoUserLoginSet(Set<User> user) {
        return user.stream().map(this::toDtoUserLogin).collect(Collectors.toSet());
    }
}
