package com.immortals.auth.service.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.immortals.auth.service.domain.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@SequenceGenerator(name = "id_seq", sequenceName = "users_seq", schema = "user_auth", allocationSize = 50)
@Table(
        name = "users",
        schema = "user_auth",
        indexes = {
                @Index(name = "idx_user_username", columnList = "user_name"),
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_contact_number", columnList = "contact_number"),
                @Index(name = "idx_user_active_ind", columnList = "active_ind")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_username", columnNames = "user_name"),
                @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_user_contact_number", columnNames = "contact_number")
        }
)
public class User extends Auditable {

    @Serial
    private static final long serialVersionUID = 1L;
    @NotBlank
    @Size(max = 50)
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    @Size(max = 50)
    @Column(name = "middle_name", length = 50)
    private String middleName;
    @NotBlank
    @Size(max = 50)
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;
    @NotBlank
    @Size(min = 3, max = 16)
    @Column(name = "user_name", nullable = false, length = 16)
    private String userName;
    @JsonIgnore
    @NotBlank
    @Size(min = 8, max = 255)
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    @Email
    @NotBlank
    @Size(max = 100)
    @Column(name = "email", nullable = false, length = 100)
    private String email;
    @Email
    @Size(max = 100)
    @Column(name = "alternate_email", length = 100)
    private String alternateEmail;
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;
    @NotBlank
    @Size(max = 5)
    @Column(name = "phone_code", nullable = false, length = 5)
    private String phoneCode;
    @NotBlank
    @Size(min = 10, max = 15)
    @Pattern(regexp = "^(\\+\\d{1,4})?[6-9][0-9]{9}$")
    @Column(name = "contact_number", nullable = false, length = 15)
    private String contactNumber;
    @Size(min = 10, max = 15)
    @Pattern(regexp = "^(\\+\\d{1,4})?[6-9][0-9]{9}$")
    @Column(name = "alternate_contact", length = 15)
    private String alternateContact;
    @Column(name = "phone_number_verified", nullable = false)
    private Boolean phoneNumberVerified;
    @Column(name = "login_time")
    private Instant loginTime;
    @Column(name = "logout_time")
    private Instant logoutTime;
    @Column(name = "account_non_expired", nullable = false)
    private Boolean accountNonExpired;
    @Column(name = "account_non_locked", nullable = false)
    private Boolean accountNonLocked;
    @Column(name = "account_locked", nullable = false)
    private Boolean accountLocked;
    @Column(name = "credentials_non_expired", nullable = false)
    private Boolean credentialsNonExpired;
    @Column(name = "active_ind", nullable = false)
    private Boolean activeInd;
    @JsonIgnore
    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<UserAddress> userAddresses;
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            schema = "user_auth",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Roles> roles;

    public User() {
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(firstName, user.firstName) && Objects.equals(middleName, user.middleName) && Objects.equals(lastName, user.lastName) && Objects.equals(userName, user.userName) && Objects.equals(password, user.password) && Objects.equals(email, user.email) && Objects.equals(alternateEmail, user.alternateEmail) && Objects.equals(emailVerified, user.emailVerified) && Objects.equals(phoneCode, user.phoneCode) && Objects.equals(contactNumber, user.contactNumber) && Objects.equals(alternateContact, user.alternateContact) && Objects.equals(phoneNumberVerified, user.phoneNumberVerified) && Objects.equals(loginTime, user.loginTime) && Objects.equals(logoutTime, user.logoutTime) && Objects.equals(accountNonExpired, user.accountNonExpired) && Objects.equals(accountNonLocked, user.accountNonLocked) && Objects.equals(accountLocked, user.accountLocked) && Objects.equals(credentialsNonExpired, user.credentialsNonExpired) && Objects.equals(activeInd, user.activeInd) && Objects.equals(userAddresses, user.userAddresses) && Objects.equals(roles, user.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, middleName, lastName, userName, password, email, alternateEmail, emailVerified, phoneCode, contactNumber, alternateContact, phoneNumberVerified, loginTime, logoutTime, accountNonExpired, accountNonLocked, accountLocked, credentialsNonExpired, activeInd, userAddresses, roles);
    }

    public String firstName() {
        return firstName;
    }

    public String middleName() {
        return middleName;
    }

    public String lastName() {
        return lastName;
    }

    public String userName() {
        return userName;
    }

    public String password() {
        return password;
    }

    public String email() {
        return email;
    }

    public String alternateEmail() {
        return alternateEmail;
    }

    public Boolean emailVerified() {
        return emailVerified;
    }

    public String phoneCode() {
        return phoneCode;
    }

    public String contactNumber() {
        return contactNumber;
    }

    public String alternateContact() {
        return alternateContact;
    }

    public Boolean phoneNumberVerified() {
        return phoneNumberVerified;
    }

    public Instant loginTime() {
        return loginTime;
    }

    public Instant logoutTime() {
        return logoutTime;
    }

    public Boolean accountNonExpired() {
        return accountNonExpired;
    }

    public Boolean accountNonLocked() {
        return accountNonLocked;
    }

    public Boolean accountLocked() {
        return accountLocked;
    }

    public Boolean credentialsNonExpired() {
        return credentialsNonExpired;
    }

    public Boolean activeInd() {
        return activeInd;
    }

    public List<UserAddress> userAddresses() {
        return userAddresses;
    }

    public Set<Roles> roles() {
        return roles;
    }

    public static final class Builder extends Auditable.Builder<User, Builder> {

        private String firstName;
        private String middleName;
        private String lastName;
        private String userName;
        private String password;
        private String email;
        private String alternateEmail;
        private Boolean emailVerified = false;
        private String phoneCode;
        private String contactNumber;
        private String alternateContact;
        private Boolean phoneNumberVerified = false;
        private Instant loginTime;
        private Instant logoutTime;
        private Boolean accountNonExpired;
        private Boolean accountNonLocked;
        private Boolean accountLocked;
        private Boolean credentialsNonExpired;
        private Boolean activeInd;
        private List<UserAddress> userAddresses;
        private Set<Roles> roles;

        private Builder() {
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return self();
        }

        public Builder middleName(String middleName) {
            this.middleName = middleName;
            return self();
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return self();
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return self();
        }

        public Builder password(String password) {
            this.password = password;
            return self();
        }

        public Builder email(String email) {
            this.email = email;
            return self();
        }

        public Builder alternateEmail(String alternateEmail) {
            this.alternateEmail = alternateEmail;
            return self();
        }

        public Builder emailVerified(Boolean emailVerified) {
            this.emailVerified = emailVerified;
            return self();
        }

        public Builder phoneCode(String phoneCode) {
            this.phoneCode = phoneCode;
            return self();
        }

        public Builder contactNumber(String contactNumber) {
            this.contactNumber = contactNumber;
            return self();
        }

        public Builder alternateContact(String alternateContact) {
            this.alternateContact = alternateContact;
            return self();
        }

        public Builder phoneNumberVerified(Boolean phoneNumberVerified) {
            this.phoneNumberVerified = phoneNumberVerified;
            return self();
        }

        public Builder loginTime(Instant loginTime) {
            this.loginTime = loginTime;
            return self();
        }

        public Builder logoutTime(Instant logoutTime) {
            this.logoutTime = logoutTime;
            return self();
        }

        public Builder accountNonExpired(Boolean accountNonExpired) {
            this.accountNonExpired = accountNonExpired;
            return self();
        }

        public Builder accountNonLocked(Boolean accountNonLocked) {
            this.accountNonLocked = accountNonLocked;
            return self();
        }

        public Builder accountLocked(Boolean accountLocked) {
            this.accountLocked = accountLocked;
            return self();
        }

        public Builder credentialsNonExpired(Boolean credentialsNonExpired) {
            this.credentialsNonExpired = credentialsNonExpired;
            return self();
        }

        public Builder activeInd(Boolean activeInd) {
            this.activeInd = activeInd;
            return self();
        }

        public Builder roles(Set<Roles> roles) {
            this.roles = roles;
            return self();
        }

        public Builder userAddresses(List<UserAddress> userAddresses) {
            this.userAddresses = userAddresses;
            return self();
        }

        @Override
        public User build() {
            User user = new User();
            applyAuditFields(user);
            user.firstName = this.firstName;
            user.middleName = this.middleName;
            user.lastName = this.lastName;
            user.userName = this.userName;
            user.password = this.password;
            user.email = this.email;
            user.alternateEmail = this.alternateEmail;
            user.emailVerified = this.emailVerified;
            user.phoneCode = this.phoneCode;
            user.contactNumber = this.contactNumber;
            user.alternateContact = this.alternateContact;
            user.phoneNumberVerified = this.phoneNumberVerified;
            user.loginTime = this.loginTime;
            user.logoutTime = this.logoutTime;
            user.accountNonExpired = this.accountNonExpired;
            user.accountNonLocked = this.accountNonLocked;
            user.accountLocked = this.accountLocked;
            user.credentialsNonExpired = this.credentialsNonExpired;
            user.activeInd = this.activeInd;
            user.roles = this.roles;
            user.userAddresses = this.userAddresses;

            return user;
        }
    }

}