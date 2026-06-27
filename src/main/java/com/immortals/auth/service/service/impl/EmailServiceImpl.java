package com.immortals.auth.service.service.impl;

import com.immortals.auth.service.service.EmailService;
import com.immortals.logging.api.ILogger;
import com.immortals.logging.core.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final ILogger log = LogManager.getInstance().getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${auth.base-url:http://localhost:8080}")
    private String baseUrl;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    @Override
    public void sendEmailVerification(String to, String token) {
        String link = baseUrl + "/api/v1/user/verify-email/confirm?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Verify your email address");
        message.setText("Click the link below to verify your email address:\n\n" + link
                + "\n\nThis link expires in 24 hours.");
        mailSender.send(message);
        log.info("Verification email sent to {}", to);
    }

    @Async
    @Override
    public void sendPasswordReset(String to, String token) {
        String link = baseUrl + "/api/v1/auth/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Reset your password");
        message.setText("Click the link below to reset your password:\n\n" + link
                + "\n\nThis link expires in 1 hour. If you did not request this, ignore this email.");
        mailSender.send(message);
        log.info("Password reset email sent to {}", to);
    }
}
