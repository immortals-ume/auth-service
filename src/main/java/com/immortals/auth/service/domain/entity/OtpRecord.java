package com.immortals.auth.service.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "otp_records", schema = "user_auth")
public class OtpRecord {

    @Id
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "otp", nullable = false, length = 6)
    private String otp;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public OtpRecord() {}

    public OtpRecord(String phoneNumber, String otp, Instant expiresAt) {
        this.phoneNumber = phoneNumber;
        this.otp = otp;
        this.expiresAt = expiresAt;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public String getOtp() { return otp; }
    public Instant getExpiresAt() { return expiresAt; }
}

