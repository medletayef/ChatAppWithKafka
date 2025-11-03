package com.example.chatapp.domain;

import static com.example.chatapp.domain.ChatRoomTestSamples.*;
import static com.example.chatapp.domain.MessageTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.chatapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MessageTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Message.class);
        Message message1 = getMessageSample1();
        Message message2 = new Message();
        assertThat(message1).isNotEqualTo(message2);

        message2.setId(message1.getId());
        assertThat(message1).isEqualTo(message2);

        message2 = getMessageSample2();
        assertThat(message1).isNotEqualTo(message2);
    }

    @Test
    void roomTest() {
        Message message = getMessageRandomSampleGenerator();
        ChatRoom chatRoomBack = getChatRoomRandomSampleGenerator();

        message.setRoom(chatRoomBack);
        assertThat(message.getRoom()).isEqualTo(chatRoomBack);

        message.room(null);
        assertThat(message.getRoom()).isNull();
    }
}
