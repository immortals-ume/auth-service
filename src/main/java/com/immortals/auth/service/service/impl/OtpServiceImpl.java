package com.immortals.auth.service.service.impl;

import com.immortals.auth.service.domain.entity.OtpRecord;
import com.immortals.auth.service.repository.OtpRecordRepository;
import com.immortals.auth.service.service.OtpService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

@Component
public class OtpServiceImpl implements OtpService {

    static final int OTP_TTL_SECONDS = 300;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpRecordRepository repository;

    public OtpServiceImpl(OtpRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public String generate(String phoneNumber) {
        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        repository.save(new OtpRecord(phoneNumber, otp, Instant.now().plusSeconds(OTP_TTL_SECONDS)));
        return otp;
    }

    @Override
    @Transactional
    public boolean verify(String phoneNumber, String otp) {
        OtpRecord record = repository.findById(phoneNumber).orElse(null);
        if (record == null || Instant.now().isAfter(record.getExpiresAt())) {
            repository.deleteById(phoneNumber);
            return false;
        }
        if (record.getOtp().equals(otp)) {
            repository.deleteById(phoneNumber);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void invalidate(String phoneNumber) {
        repository.deleteById(phoneNumber);
    }

    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void evictExpired() {
        repository.deleteByExpiresAtBefore(Instant.now());
    }
}

