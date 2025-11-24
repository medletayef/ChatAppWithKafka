package com.example.chatapp.service.dto;

import com.example.chatapp.domain.AbstractAuditingEntity;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public class ChatRoomSummaryDto extends AbstractAuditingEntity implements Serializable {

    private Long id;

    private String name;

    private String lastMessage;

    private Instant lastMsgSentAt;

    private String sender;

    private String senderImageUrl;

    private Set<UserDTO> members;

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

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Instant getLastMsgSentAt() {
        return lastMsgSentAt;
    }

    public void setLastMsgSentAt(Instant lastMsgSentAt) {
        this.lastMsgSentAt = lastMsgSentAt;
    }

    public String getSenderImageUrl() {
        return senderImageUrl;
    }

    public void setSenderImageUrl(String senderImageUrl) {
        this.senderImageUrl = senderImageUrl;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Set<UserDTO> getMembers() {
        return members;
    }

    public void setMembers(Set<UserDTO> members) {
        this.members = members;
    }

    public ChatRoomSummaryDto() {}

    public ChatRoomSummaryDto(
        Long id,
        String name,
        String lastMessage,
        Instant lastMsgSentAt,
        String sender,
        String senderImageUrl,
        Set<UserDTO> members
    ) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.lastMsgSentAt = lastMsgSentAt;
        this.sender = sender;
        this.senderImageUrl = senderImageUrl;
        this.members = members;
    }
}
