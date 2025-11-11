package com.example.chatapp.repository;

import com.example.chatapp.domain.ChatRoom;
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
        SELECT DISTINCT c
        FROM ChatRoom c
        inner JOIN c.members m
        WHERE (c.createdBy = :creatorLogin and m.id = :memberId ) or (c.createdBy = :memberLogin and m.login = :creatorLogin)
        """
    )
    List<ChatRoom> findAllRelatedRooms(
        @Param("memberId") Long memberId,
        @Param("memberLogin") String memberLogin,
        @Param("creatorLogin") String creatorLogin
    );
}
