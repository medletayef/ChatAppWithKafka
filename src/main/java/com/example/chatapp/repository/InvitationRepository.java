package com.example.chatapp.repository;

import com.example.chatapp.domain.Invitation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    @Query(
        """
            SELECT i FROM Invitation i
            WHERE i.user.login = :login
        """
    )
    Page<Invitation> findByUserLogin(@Param("login") String login, Pageable pageable);
}
