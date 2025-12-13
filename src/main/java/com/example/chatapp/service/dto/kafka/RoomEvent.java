package com.example.chatapp.service.dto.kafka;

import java.io.Serializable;
import java.util.Set;

public class RoomEvent implements Serializable {

    public enum RoomEventType {
        INVITATION_SENT("INVITATION_SENT"),
        ROOM_JOINED("ROOM_JOINED"),
        ROOM_REJECTED("ROOM_REJECTED"),
        ROOM_LEFT("ROOM_LEFT"),
        ROOM_DELETED("ROOM_DELETED");

        RoomEventType(String room_joined) {}
    }

    private Long roomId;
    private String roomName;
    private RoomEventType type;
    private String sender;

    private String receiver;
    private Set<String> recipients;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public RoomEventType getType() {
        return type;
    }

    public void setType(RoomEventType type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Set<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(Set<String> recipients) {
        this.recipients = recipients;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}
