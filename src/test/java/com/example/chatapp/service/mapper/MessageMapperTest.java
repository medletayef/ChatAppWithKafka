package com.example.chatapp.service.mapper;

import static com.example.chatapp.domain.MessageAsserts.*;
import static com.example.chatapp.domain.MessageTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessageMapperTest {

    private MessageMapper messageMapper;

    @BeforeEach
    void setUp() {
        messageMapper = new MessageMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getMessageSample1();
        var actual = messageMapper.toEntity(messageMapper.toDto(expected));
        assertMessageAllPropertiesEquals(expected, actual);
    }
}
