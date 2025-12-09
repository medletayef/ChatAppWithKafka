package com.example.chatapp.repository;

import com.example.chatapp.domain.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByRoom_IdAndUser_Id(Long roomId, Long userId);
    void deleteAllByRoom_Id(Long roomId);
}
