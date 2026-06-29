package com.immortals.auth.service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.acks:all}")
    private String acks;

    @Value("${spring.kafka.producer.retries:3}")
    private int retries;

    @Value("${auth.kafka.topics.email-verification}")
    private String emailVerificationTopic;

    @Value("${auth.kafka.topics.password-reset}")
    private String passwordResetTopic;

    @Value("${auth.kafka.topics.otp}")
    private String otpTopic;

    @Value("${auth.kafka.partitions:3}")
    private int partitions;

    @Value("${auth.kafka.replication-factor:1}")
    private short replicationFactor;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, acks);
        config.put(ProducerConfig.RETRIES_CONFIG, retries);
        config.put(JacksonJsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public NewTopic emailVerificationTopic() {
        return TopicBuilder.name(emailVerificationTopic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    public NewTopic passwordResetTopic() {
        return TopicBuilder.name(passwordResetTopic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    public NewTopic otpTopic() {
        return TopicBuilder.name(otpTopic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }
}

