package com.example.chatapp.service.mapper;

import com.example.chatapp.domain.ChatRoom;
import com.example.chatapp.domain.Message;
import com.example.chatapp.domain.User;
import com.example.chatapp.service.dto.ChatRoomDTO;
import com.example.chatapp.service.dto.MessageDTO;
import com.example.chatapp.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Message} and its DTO {@link MessageDTO}.
 */
@Mapper(componentModel = "spring", uses = { ChatRoomMapper.class, UserMapper.class })
public interface MessageMapper extends EntityMapper<MessageDTO, Message> {
    @Mapping(target = "sender", source = "sender", qualifiedByName = "userLogin")
    @Mapping(target = "room", source = "room", qualifiedByName = "chatRoomId")
    MessageDTO toDto(Message s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("chatRoomId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ChatRoomDTO toDtoChatRoomId(ChatRoom chatRoom);
}
