package com.immortals.auth.service.event;

import com.immortals.logging.api.ILogger;
import com.immortals.logging.core.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Component
public class NotificationEventPublisher {

    private static final ILogger log = LogManager.getInstance().getLogger(NotificationEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${auth.kafka.topics.email-verification}")
    private String emailVerificationTopic;

    @Value("${auth.kafka.topics.password-reset}")
    private String passwordResetTopic;

    @Value("${auth.kafka.topics.otp}")
    private String otpTopic;

    public NotificationEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishEmailVerification(String email, String verificationLink) {
        EmailVerificationRequestedEvent event = new EmailVerificationRequestedEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                email,
                verificationLink
        );
        send(emailVerificationTopic, email, event);
    }

    public void publishPasswordReset(String email, String resetLink) {
        PasswordResetRequestedEvent event = new PasswordResetRequestedEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                email,
                resetLink
        );
        send(passwordResetTopic, email, event);
    }

    public void publishOtpRequested(String phoneNumber, String otp, int ttlSeconds) {
        OtpRequestedEvent event = new OtpRequestedEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                phoneNumber,
                otp,
                ttlSeconds
        );
        send(otpTopic, phoneNumber, event);
    }

    private void send(String topic, String key, Object event) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event to topic '{}' key='{}': {}", topic, key, ex.getMessage(), ex);
            } else {
                log.debug("Event published to topic='{}' partition={} offset={}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}

