package com.example.chatapp.service.dto;

import com.example.chatapp.domain.enumeration.InvitationStatus;
import java.io.Serializable;

public class InvitationDTO implements Serializable {

    private Long id;

    private InvitationStatus status;

    private ChatRoomDTO chatRoom;

    private UserDTO user;

    public InvitationDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public void setStatus(InvitationStatus status) {
        this.status = status;
    }

    public ChatRoomDTO getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(ChatRoomDTO chatRoom) {
        this.chatRoom = chatRoom;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }
}
