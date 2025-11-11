package com.example.chatapp.service.dto.kafka;

import java.time.Instant;

public class UserStatusEvent {

    public enum State {
        ACTIVE,
        ABSENT,
        OFFLINE,
    }

    private String userId;

    private String fullName;

    private String imageUrl;
    private State state;
    private Instant timestamp;
    private String sessionId; // optional

    public UserStatusEvent() {}

    public UserStatusEvent(String userId, State state, Instant timestamp, String sessionId) {
        this.userId = userId;
        this.state = state;
        this.timestamp = timestamp;
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
