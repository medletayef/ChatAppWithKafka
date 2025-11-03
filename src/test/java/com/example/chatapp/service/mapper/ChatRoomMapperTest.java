package com.example.chatapp.service.mapper;

import static com.example.chatapp.domain.ChatRoomAsserts.*;
import static com.example.chatapp.domain.ChatRoomTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChatRoomMapperTest {

    private ChatRoomMapper chatRoomMapper;

    @BeforeEach
    void setUp() {
        chatRoomMapper = new ChatRoomMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getChatRoomSample1();
        var actual = chatRoomMapper.toEntity(chatRoomMapper.toDto(expected));
        assertChatRoomAllPropertiesEquals(expected, actual);
    }
}
