package com.example.chatapp.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A ChatRoom.
 */
@Entity
@Table(name = "chat_room")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ChatRoom extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_chat_room__members",
        joinColumns = @JoinColumn(name = "chat_room_id"),
        inverseJoinColumns = @JoinColumn(name = "members_id")
    )
    private Set<User> members = new HashSet<>();

    @Column(name = "last_msg_sent_at", nullable = true)
    private Instant lastMsgSentAt;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ChatRoom id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public ChatRoom name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<User> getMembers() {
        return this.members;
    }

    public void setMembers(Set<User> users) {
        this.members = users;
    }

    public ChatRoom members(Set<User> users) {
        this.setMembers(users);
        return this;
    }

    public ChatRoom addMembers(User user) {
        this.members.add(user);
        return this;
    }

    public ChatRoom removeMembers(User user) {
        this.members.remove(user);
        return this;
    }

    public Instant getLastMsgSentAt() {
        return lastMsgSentAt;
    }

    public void setLastMsgSentAt(Instant lastMsgSentAt) {
        this.lastMsgSentAt = lastMsgSentAt;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChatRoom)) {
            return false;
        }
        return getId() != null && getId().equals(((ChatRoom) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }
    // prettier-ignore

}
