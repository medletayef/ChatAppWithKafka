package com.example.chatapp.service;

import com.example.chatapp.service.dto.ChatRoomDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.example.chatapp.domain.ChatRoom}.
 */
public interface ChatRoomService {
    /**
     * Save a chatRoom.
     *
     * @param chatRoomDTO the entity to save.
     * @return the persisted entity.
     */
    ChatRoomDTO save(ChatRoomDTO chatRoomDTO);

    /**
     * Updates a chatRoom.
     *
     * @param chatRoomDTO the entity to update.
     * @return the persisted entity.
     */
    ChatRoomDTO update(ChatRoomDTO chatRoomDTO);

    /**
     * Partially updates a chatRoom.
     *
     * @param chatRoomDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ChatRoomDTO> partialUpdate(ChatRoomDTO chatRoomDTO);

    /**
     * Get all the chatRooms.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ChatRoomDTO> findAll(Pageable pageable);

    /**
     * Get all the chatRooms with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ChatRoomDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" chatRoom.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ChatRoomDTO> findOne(Long id);

    /**
     * Delete the "id" chatRoom.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
