package com.example.chatapp.service.mapper;

import com.example.chatapp.domain.ChatRoom;
import com.example.chatapp.domain.User;
import com.example.chatapp.service.dto.ChatRoomDTO;
import com.example.chatapp.service.dto.UserDTO;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for the entity {@link ChatRoom} and its DTO {@link ChatRoomDTO}.
 */
@Mapper(componentModel = "spring", uses = { /* Maybe UserService.class or UserRepository.class for robust mapping */ })
public abstract class ChatRoomMapper implements EntityMapper<ChatRoomDTO, ChatRoom> {

    // NOTE: In a real JHipster app, you'd inject a UserRepository/UserService here
    // to fetch the actual User entities based on the login string, instead of
    // creating new User objects only containing the login field.

    // Entity → DTO
    @Mapping(target = "members", source = "members", qualifiedByName = "userLoginSet")
    public abstract ChatRoomDTO toDto(ChatRoom chatRoom);

    // DTO → Entity
    // MapStruct will now automatically find the un-named 'Set<User> map(Set<String> logins)' method.
    @Mapping(target = "removeMembers", ignore = true)
    public abstract ChatRoom toEntity(ChatRoomDTO chatRoomDTO);

    // --- Custom mapping methods ---

    // 1. Entity → DTO: Set<User> → Set<String> (Kept as is, using @Named)
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
        // WARNING: This implementation is usually insufficient for DTO-to-Entity mapping,
        // as it creates User objects that only have the 'login' field set.
        // For partialUpdate, you typically need to fetch the existing User entities from the DB.
        return logins
            .stream()
            .map(login -> {
                User user = new User();
                // TODO: In a real app, you should look up the User entity here using a service/repository.
                user.setLogin(login);
                return user;
            })
            .collect(Collectors.toSet());
    }
}
