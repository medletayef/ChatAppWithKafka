package com.example.chatapp.web.rest;

import static com.example.chatapp.domain.ChatRoomAsserts.*;
import static com.example.chatapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.chatapp.IntegrationTest;
import com.example.chatapp.domain.ChatRoom;
import com.example.chatapp.repository.ChatRoomRepository;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.service.ChatRoomService;
import com.example.chatapp.service.dto.ChatRoomDTO;
import com.example.chatapp.service.mapper.ChatRoomMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ChatRoomResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ChatRoomResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/chat-rooms";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private ChatRoomRepository chatRoomRepositoryMock;

    @Autowired
    private ChatRoomMapper chatRoomMapper;

    @Mock
    private ChatRoomService chatRoomServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restChatRoomMockMvc;

    private ChatRoom chatRoom;

    private ChatRoom insertedChatRoom;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ChatRoom createEntity() {
        return new ChatRoom().name(DEFAULT_NAME).createdAt(DEFAULT_CREATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ChatRoom createUpdatedEntity() {
        return new ChatRoom().name(UPDATED_NAME).createdAt(UPDATED_CREATED_AT);
    }

    @BeforeEach
    public void initTest() {
        chatRoom = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedChatRoom != null) {
            chatRoomRepository.delete(insertedChatRoom);
            insertedChatRoom = null;
        }
    }

    @Test
    @Transactional
    void createChatRoom() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ChatRoom
        ChatRoomDTO chatRoomDTO = chatRoomMapper.toDto(chatRoom);
        var returnedChatRoomDTO = om.readValue(
            restChatRoomMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(chatRoomDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ChatRoomDTO.class
        );

        // Validate the ChatRoom in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedChatRoom = chatRoomMapper.toEntity(returnedChatRoomDTO);
        assertChatRoomUpdatableFieldsEquals(returnedChatRoom, getPersistedChatRoom(returnedChatRoom));

        insertedChatRoom = returnedChatRoom;
    }

    @Test
    @Transactional
    void createChatRoomWithExistingId() throws Exception {
        // Create the ChatRoom with an existing ID
        chatRoom.setId(1L);
        ChatRoomDTO chatRoomDTO = chatRoomMapper.toDto(chatRoom);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restChatRoomMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(chatRoomDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ChatRoom in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        chatRoom.setName(null);

        // Create the ChatRoom, which fails.
        ChatRoomDTO chatRoomDTO = chatRoomMapper.toDto(chatRoom);

        restChatRoomMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(chatRoomDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        chatRoom.setCreatedAt(null);

        // Create the ChatRoom, which fails.
        ChatRoomDTO chatRoomDTO = chatRoomMapper.toDto(chatRoom);

        restChatRoomMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(chatRoomDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllChatRooms() throws Exception {
        // Initialize the database
        insertedChatRoom = chatRoomRepository.saveAndFlush(chatRoom);

        // Get all the chatRoomList
        restChatRoomMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(chatRoom.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllChatRoomsWithEagerRelationshipsIsEnabled() throws Exception {
        when(chatRoomServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restChatRoomMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(chatRoomServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllChatRoomsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(chatRoomServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restChatRoomMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(chatRoomRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getChatRoom() throws Exception {
        // Initialize the database
        insertedChatRoom = chatRoomRepository.saveAndFlush(chatRoom);

        // Get the chatRoom
        restChatRoomMockMvc
            .perform(get(ENTITY_API_URL_ID, chatRoom.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(chatRoom.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingChatRoom() throws Exception {
        // Get the chatRoom
        restChatRoomMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingChatRoom() throws Exception {
        // Initialize the database
        insertedChatRoom = chatRoomRepository.saveAndFlush(chatRoom);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the chatRoom
        ChatRoom updatedChatRoom = chatRoomRepository.findById(chatRoom.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedChatRoom are not directly saved in db
        em.detach(updatedChatRoom);
        updatedChatRoom.name(UPDATED_NAME).createdAt(UPDATED_CREATED_AT);
        ChatRoomDTO chatRoomDTO = chatRoomMapper.toDto(updatedChatRoom);

        restChatRoomMockMvc
            .perform(
                put(ENTITY_API_URL_ID, chatRoomDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(chatRoomDTO))
            )
            .andExpect(status().isOk());

        // Validate the ChatRoom in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedChatRoomToMatchAllProperties(updatedChatRoom);
    }

    @Test
    @Transactional
    void putNonExistingChatRoom() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        chatRoom.setId(longCount.incrementAndGet());

        // Create the ChatRoom
        ChatRoomDTO chatRoomDTO = chatRoomMapper.toDto(chatRoom);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restChatRoomMockMvc
            .perform(
                put(ENTITY_API_URL_ID, chatRoomDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(chatRoomDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ChatRoom in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchChatRoom() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        chatRoom.setId(longCount.incrementAndGet());

        // Create the ChatRoom
        ChatRoomDTO chatRoomDTO = chatRoomMapper.toDto(chatRoom);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChatRoomMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(chatRoomDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ChatRoom in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamChatRoom() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        chatRoom.setId(longCount.incrementAndGet());

        // Create the ChatRoom
        ChatRoomDTO chatRoomDTO = chatRoomMapper.toDto(chatRoom);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChatRoomMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(chatRoomDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ChatRoom in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateChatRoomWithPatch() throws Exception {
        // Initialize the database
        insertedChatRoom = chatRoomRepository.saveAndFlush(chatRoom);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the chatRoom using partial update
        ChatRoom partialUpdatedChatRoom = new ChatRoom();
        partialUpdatedChatRoom.setId(chatRoom.getId());

        partialUpdatedChatRoom.name(UPDATED_NAME);

        restChatRoomMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedChatRoom.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedChatRoom))
            )
            .andExpect(status().isOk());

        // Validate the ChatRoom in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertChatRoomUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedChatRoom, chatRoom), getPersistedChatRoom(chatRoom));
    }

    @Test
    @Transactional
    void fullUpdateChatRoomWithPatch() throws Exception {
        // Initialize the database
        insertedChatRoom = chatRoomRepository.saveAndFlush(chatRoom);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the chatRoom using partial update
        ChatRoom partialUpdatedChatRoom = new ChatRoom();
        partialUpdatedChatRoom.setId(chatRoom.getId());

        partialUpdatedChatRoom.name(UPDATED_NAME).createdAt(UPDATED_CREATED_AT);

        restChatRoomMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedChatRoom.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedChatRoom))
            )
            .andExpect(status().isOk());

        // Validate the ChatRoom in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertChatRoomUpdatableFieldsEquals(partialUpdatedChatRoom, getPersistedChatRoom(partialUpdatedChatRoom));
    }

    @Test
    @Transactional
    void patchNonExistingChatRoom() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        chatRoom.setId(longCount.incrementAndGet());

        // Create the ChatRoom
        ChatRoomDTO chatRoomDTO = chatRoomMapper.toDto(chatRoom);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restChatRoomMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, chatRoomDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(chatRoomDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ChatRoom in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchChatRoom() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        chatRoom.setId(longCount.incrementAndGet());

        // Create the ChatRoom
        ChatRoomDTO chatRoomDTO = chatRoomMapper.toDto(chatRoom);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChatRoomMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(chatRoomDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ChatRoom in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamChatRoom() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        chatRoom.setId(longCount.incrementAndGet());

        // Create the ChatRoom
        ChatRoomDTO chatRoomDTO = chatRoomMapper.toDto(chatRoom);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChatRoomMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(chatRoomDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ChatRoom in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteChatRoom() throws Exception {
        // Initialize the database
        insertedChatRoom = chatRoomRepository.saveAndFlush(chatRoom);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the chatRoom
        restChatRoomMockMvc
            .perform(delete(ENTITY_API_URL_ID, chatRoom.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return chatRoomRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected ChatRoom getPersistedChatRoom(ChatRoom chatRoom) {
        return chatRoomRepository.findById(chatRoom.getId()).orElseThrow();
    }

    protected void assertPersistedChatRoomToMatchAllProperties(ChatRoom expectedChatRoom) {
        assertChatRoomAllPropertiesEquals(expectedChatRoom, getPersistedChatRoom(expectedChatRoom));
    }

    protected void assertPersistedChatRoomToMatchUpdatableProperties(ChatRoom expectedChatRoom) {
        assertChatRoomAllUpdatablePropertiesEquals(expectedChatRoom, getPersistedChatRoom(expectedChatRoom));
    }
}
