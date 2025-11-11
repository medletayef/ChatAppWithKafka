package com.example.chatapp.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link com.example.chatapp.domain.ChatRoom} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ChatRoomDTO implements Serializable {

    private Long id;

    @NotNull
    private String name;

    @NotNull
    private Set<String> members = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChatRoomDTO)) {
            return false;
        }

        ChatRoomDTO chatRoomDTO = (ChatRoomDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, chatRoomDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore

    public Set<String> getMembers() {
        return members;
    }

    public void setMembers(Set<String> members) {
        this.members = members;
    }
}
