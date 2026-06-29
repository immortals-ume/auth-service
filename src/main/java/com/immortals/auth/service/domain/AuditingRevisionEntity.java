package com.immortals.auth.service.domain;

import com.immortals.auth.service.audit.AuditingRevisionListener;
import jakarta.persistence.*;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "revinfo", schema = "user_audit")
@RevisionEntity(AuditingRevisionListener.class)
public class AuditingRevisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "revinfo_seq")
    @SequenceGenerator(name = "revinfo_seq", sequenceName = "revinfo_seq", schema = "user_audit", allocationSize = 1)
    @RevisionNumber
    private int id;

    @RevisionTimestamp
    private long timestamp;

    private String username;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Transient
    public Date getRevisionDate() {
        return new Date(timestamp);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AuditingRevisionEntity that = (AuditingRevisionEntity) o;
        return id == that.id && timestamp == that.timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timestamp);
    }

    @Override
    public String toString() {
        return "AuditingRevisionEntity{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", username='" + username + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                '}';
    }
}

