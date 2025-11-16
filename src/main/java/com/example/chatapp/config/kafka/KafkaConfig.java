package com.example.chatapp.config.kafka;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        //        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,"5000");
        //        props.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG,"5000");
        //        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,"10000");
        //        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG,"3000");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.chatapp.service.dto.kafka");
        return props;
    }

    // 🔹 Generic ProducerFactory for any value type
    public <T> ProducerFactory<String, T> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    // 🔹 Generic KafkaTemplate for any value type
    public <T> KafkaTemplate<String, T> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // 🔹 Generic ConsumerFactory for any value type
    public <T> ConsumerFactory<String, T> consumerFactory(Class<T> valueType) {
        JsonDeserializer<T> deserializer = new JsonDeserializer<>(valueType);
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(), deserializer);
    }

    // 🔹 Generic listener factory (for @KafkaListener)
    public <T> ConcurrentKafkaListenerContainerFactory<String, T> kafkaListenerContainerFactory(Class<T> valueType) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(valueType));
        return factory;
    }

    @Bean
    public KafkaAdmin.NewTopics chatTopics() {
        return new KafkaAdmin.NewTopics(TopicBuilder.name("user-status").partitions(3).replicas(1).build());
    }
}
