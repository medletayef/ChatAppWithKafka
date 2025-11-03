package com.example.chatapp.domain;

import static com.example.chatapp.domain.ChatRoomTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.chatapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ChatRoomTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ChatRoom.class);
        ChatRoom chatRoom1 = getChatRoomSample1();
        ChatRoom chatRoom2 = new ChatRoom();
        assertThat(chatRoom1).isNotEqualTo(chatRoom2);

        chatRoom2.setId(chatRoom1.getId());
        assertThat(chatRoom1).isEqualTo(chatRoom2);

        chatRoom2 = getChatRoomSample2();
        assertThat(chatRoom1).isNotEqualTo(chatRoom2);
    }
}
