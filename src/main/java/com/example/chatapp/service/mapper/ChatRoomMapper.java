package com.example.chatapp.service.mapper;

import com.example.chatapp.domain.ChatRoom;
import com.example.chatapp.domain.User;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.service.dto.ChatRoomDTO;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Mapper for the entity {@link ChatRoom} and its DTO {@link ChatRoomDTO}.
 */
@Mapper(componentModel = "spring")
public abstract class ChatRoomMapper implements EntityMapper<ChatRoomDTO, ChatRoom> {

    @Autowired
    private UserRepository userRepository;

    // Entity → DTO
    @Mapping(target = "members", source = "members", qualifiedByName = "userLoginSet")
    public abstract ChatRoomDTO toDto(ChatRoom chatRoom);

    @Mapping(target = "removeMembers", ignore = true)
    public abstract ChatRoom toEntity(ChatRoomDTO chatRoomDTO);

    @Named("userLoginSet")
    public Set<String> mapUserToLoginSet(Set<User> users) {
        if (users == null) return new HashSet<>();
        return users.stream().map(User::getLogin).collect(Collectors.toSet());
    }

    // 2. DTO → Entity: Set<String> → Set<User>
    // **FIX**: This method signature is now un-named (no @Named) and uses the standard 'map' name.
    // MapStruct will use this automatically for toEntity() and partialUpdate().
    public Set<User> map(Set<String> logins) {
        if (logins == null) return new HashSet<>();
        return logins
            .stream()
            .map(userRepository::findOneByLogin) // Assumes findOneByLogin returns Optional<User>
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
    }
}
