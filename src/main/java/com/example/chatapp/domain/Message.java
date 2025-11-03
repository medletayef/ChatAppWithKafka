package com.example.chatapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Message.
 */
@Entity
@Table(name = "message")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "content", nullable = false)
    private String content;

    @NotNull
    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "members" }, allowSetters = true)
    private ChatRoom room;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Message id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return this.content;
    }

    public Message content(String content) {
        this.setContent(content);
        return this;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getSentAt() {
        return this.sentAt;
    }

    public Message sentAt(Instant sentAt) {
        this.setSentAt(sentAt);
        return this;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public User getSender() {
        return this.sender;
    }

    public void setSender(User user) {
        this.sender = user;
    }

    public Message sender(User user) {
        this.setSender(user);
        return this;
    }

    public ChatRoom getRoom() {
        return this.room;
    }

    public void setRoom(ChatRoom chatRoom) {
        this.room = chatRoom;
    }

    public Message room(ChatRoom chatRoom) {
        this.setRoom(chatRoom);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Message)) {
            return false;
        }
        return getId() != null && getId().equals(((Message) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Message{" +
            "id=" + getId() +
            ", content='" + getContent() + "'" +
            ", sentAt='" + getSentAt() + "'" +
            "}";
    }
}
