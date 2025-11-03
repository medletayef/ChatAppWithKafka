package com.example.chatapp.repository;

import com.example.chatapp.domain.ChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface ChatRoomRepositoryWithBagRelationships {
    Optional<ChatRoom> fetchBagRelationships(Optional<ChatRoom> chatRoom);

    List<ChatRoom> fetchBagRelationships(List<ChatRoom> chatRooms);

    Page<ChatRoom> fetchBagRelationships(Page<ChatRoom> chatRooms);
}
