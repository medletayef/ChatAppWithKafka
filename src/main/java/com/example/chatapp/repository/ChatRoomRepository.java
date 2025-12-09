package com.example.chatapp.repository;

import com.example.chatapp.domain.ChatRoom;
import com.example.chatapp.domain.Message;
import com.example.chatapp.service.dto.ChatRoomSummaryDto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ChatRoom entity.
 *
 * When extending this class, extend ChatRoomRepositoryWithBagRelationships too.
 * For more information refer to https://github.com/jhipster/generator-jhipster/issues/17990.
 */
@Repository
public interface ChatRoomRepository extends ChatRoomRepositoryWithBagRelationships, JpaRepository<ChatRoom, Long> {
    default Optional<ChatRoom> findOneWithEagerRelationships(Long id) {
        return this.fetchBagRelationships(this.findById(id));
    }

    default List<ChatRoom> findAllWithEagerRelationships() {
        return this.fetchBagRelationships(this.findAll());
    }

    default Page<ChatRoom> findAllWithEagerRelationships(Pageable pageable) {
        return this.fetchBagRelationships(this.findAll(pageable));
    }

    @Query(
        """
        SELECT m
        FROM Message m
        where m.room.id = :roomId
        ORDER BY m.sentAt DESC
        """
    )
    List<Message> findLastMessageOfRoom(@Param("roomId") Long roomId, Pageable pageable);

    @Query(
        """
        SELECT c
        FROM ChatRoom c
        INNER JOIN c.members m
        WHERE
            (c.createdBy = :creatorLogin AND m.login = :memberLogin)
         OR (c.createdBy = :memberLogin AND m.login = :creatorLogin)
             GROUP BY (c.id)
        ORDER BY  CASE WHEN c.lastMsgSentAt IS NULL THEN 1 ELSE 0 END, c.lastMsgSentAt DESC

        """
    )
    Page<ChatRoom> findRecentRelatedRoomsToMember(
        @Param("memberLogin") String memberLogin,
        @Param("creatorLogin") String creatorLogin,
        Pageable pageable
    );

    @Query(
        """
        SELECT c
        FROM ChatRoom c
        INNER JOIN c.members m
        WHERE
            (c.createdBy = :creatorLogin or m.login = :creatorLogin)
            GROUP BY (c.id)
        ORDER BY   CASE WHEN c.lastMsgSentAt IS NULL THEN 1 ELSE 0 END, c.lastMsgSentAt DESC

        """
    )
    Page<ChatRoom> findRecentRelatedRooms(@Param("creatorLogin") String creatorLogin, Pageable pageable);

    @Query(
        """
        SELECT c
        FROM ChatRoom c
        INNER JOIN c.members m
        WHERE
            (c.createdBy = :memberLogin or m.login = :memberLogin) AND (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))
            GROUP BY (c.id)
        ORDER BY   CASE WHEN c.lastMsgSentAt IS NULL THEN 1 ELSE 0 END, c.lastMsgSentAt DESC

        """
    )
    Page<ChatRoom> findByNameLike(@Param("name") String name, @Param("memberLogin") String memberLogin, Pageable pageable);
}
