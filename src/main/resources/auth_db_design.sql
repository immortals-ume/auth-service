CREATE DATABASE user_db;

CREATE SCHEMA IF NOT EXISTS user_auth;

CREATE SCHEMA IF NOT EXISTS user_auth;

CREATE SCHEMA IF NOT EXISTS user_session;

CREATE SCHEMA IF NOT EXISTS user_oauth;

CREATE SCHEMA IF NOT EXISTS user_audit;

CREATE SCHEMA IF NOT EXISTS master_data;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

--  Main User Data Table
CREATE SEQUENCE IF NOT EXISTS user_auth.id_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE user_auth.users
(
    id                      BIGINT             NOT NULL DEFAULT nextval('user_auth.id_seq'),
    version                 BIGINT             NOT NULL DEFAULT 0,
    first_name              varchar(50)        not null,
    middle_name             varchar(50),
    last_name               varchar(50)        not null,
    user_name               varchar(16) unique not null,
    password                varchar            not null,
    email                   varchar(100)       not null,
    alternate_email         varchar(100)       not null,
    phoneCode               varchar(5),
    contact_number          varchar(15),
    alternate_contact       varchar(15),
    phone_number_verified   bool default false not null,
    account_non_expired     bool default true  not null,
    account_non_locked      bool default true  not null,
    credentials_non_expired bool default true  not null,
    active_ind              bool default true  not null,
    created_date            timestamp          not null,
    created_by              varchar            not null,
    updated_by              varchar,
    updated_date            timestamp,
    deleted_by              varchar,
    deleted_date            timestamp,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_user_username UNIQUE (user_name),
    CONSTRAINT uk_user_email UNIQUE (email),
    CONSTRAINT uk_user_contact_number UNIQUE (contact_number)
);

CREATE INDEX idx_user_username
    ON user_auth.users (user_name);

CREATE INDEX idx_user_email
    ON user_auth.users (email);

CREATE INDEX idx_user_contact_number
    ON user_auth.users (contact_number);

CREATE INDEX idx_user_active_ind
    ON user_auth.users (active_ind);

--  Main User Address Data Table
CREATE TABLE user_auth.user_address
(
    id                BIGINT       NOT NULL DEFAULT nextval('user_auth.id_seq'),
    version           BIGINT       NOT NULL DEFAULT 0,
    address_uuid      VARCHAR(36)  NOT NULL,
    label             VARCHAR(50),
    address_line1     VARCHAR(255) NOT NULL,
    address_line2     VARCHAR(255),
    landmark          VARCHAR(255),
    pincode           VARCHAR(6)   NOT NULL,
    address_type      VARCHAR(20)  NOT NULL,
    is_default        BOOLEAN      NOT NULL DEFAULT FALSE,
    status            VARCHAR(20)  NOT NULL,
    is_verified       BOOLEAN      NOT NULL DEFAULT FALSE,
    is_po_box         BOOLEAN      NOT NULL DEFAULT FALSE,
    timezone          VARCHAR(50),
    language_code     VARCHAR(5),
    formatted_address TEXT,
    latitude          NUMERIC(10, 7),
    longitude         NUMERIC(10, 7),
    user_id           BIGINT       NOT NULL,
    country_id        BIGINT       NOT NULL,
    state_id          BIGINT       NOT NULL,
    city_id           BIGINT       NOT NULL,
    created_by        VARCHAR(255) NOT NULL,
    created_date      TIMESTAMP    NOT NULL,
    updated_by        VARCHAR(255),
    updated_date      TIMESTAMP,
    deleted_by        VARCHAR(255),
    deleted_date      TIMESTAMP,

    CONSTRAINT pk_user_address PRIMARY KEY (id),
    CONSTRAINT uk_user_address_uuid
        UNIQUE (address_uuid),
    CONSTRAINT fk_user_address_user
        FOREIGN KEY (user_id)
            REFERENCES user_auth.users (id),
    CONSTRAINT fk_user_address_country
        FOREIGN KEY (country_id)
            REFERENCES master_data.countries (id),
    CONSTRAINT fk_user_address_state
        FOREIGN KEY (state_id)
            REFERENCES master_data.states (id),
    CONSTRAINT fk_user_address_city
        FOREIGN KEY (city_id)
            REFERENCES master_data.cities (id),
    CONSTRAINT chk_user_address_pincode
        CHECK (pincode ~ '^[0-9]{6}$')
);

CREATE INDEX idx_user_address_user_id
    ON user_auth.user_address (user_id);

CREATE INDEX idx_user_address_country_id
    ON user_auth.user_address (country_id);

CREATE INDEX idx_user_address_state_id
    ON user_auth.user_address (state_id);

CREATE INDEX idx_user_address_city_id
    ON user_auth.user_address (city_id);

CREATE INDEX idx_user_address_pincode
    ON user_auth.user_address (pincode);


--  Master Data
CREATE TABLE master_data.countries
(
    id           BIGINT       NOT NULL DEFAULT nextval('user_auth.id_seq'),
    version      BIGINT       NOT NULL DEFAULT 0,

    country_name VARCHAR(100) NOT NULL,
    country_code VARCHAR(5)   NOT NULL,

    active_ind   BOOLEAN      NOT NULL DEFAULT TRUE,

    created_by   VARCHAR(255) NOT NULL,
    created_date TIMESTAMP    NOT NULL,

    updated_by   VARCHAR(255),
    updated_date TIMESTAMP,

    deleted_by   VARCHAR(255),
    deleted_date TIMESTAMP,

    CONSTRAINT pk_countries PRIMARY KEY (id),

    CONSTRAINT uk_countries_name
        UNIQUE (country_name),

    CONSTRAINT uk_countries_code
        UNIQUE (country_code)
);

CREATE INDEX idx_countries_name
    ON master_data.countries (country_name);

CREATE INDEX idx_countries_code
    ON master_data.countries (country_code);

CREATE TABLE master_data.states
(
    id           BIGINT       NOT NULL DEFAULT nextval('user_auth.id_seq'),
    version      BIGINT       NOT NULL DEFAULT 0,

    state_name   VARCHAR(100) NOT NULL,
    state_code   VARCHAR(10)  NOT NULL,

    country_id   BIGINT       NOT NULL,

    active_ind   BOOLEAN      NOT NULL DEFAULT TRUE,

    created_by   VARCHAR(255) NOT NULL,
    created_date TIMESTAMP    NOT NULL,

    updated_by   VARCHAR(255),
    updated_date TIMESTAMP,

    deleted_by   VARCHAR(255),
    deleted_date TIMESTAMP,

    CONSTRAINT pk_states PRIMARY KEY (id),

    CONSTRAINT uk_states_name_country
        UNIQUE (state_name, country_id),

    CONSTRAINT fk_states_country
        FOREIGN KEY (country_id)
            REFERENCES master_data.countries (id)
);

CREATE INDEX idx_states_country_id
    ON master_data.states (country_id);

CREATE INDEX idx_states_name
    ON master_data.states (state_name);

CREATE TABLE master_data.cities
(
    id           BIGINT       NOT NULL DEFAULT nextval('user_auth.id_seq'),
    version      BIGINT       NOT NULL DEFAULT 0,

    city_name    VARCHAR(100) NOT NULL,

    state_id     BIGINT       NOT NULL,

    active_ind   BOOLEAN      NOT NULL DEFAULT TRUE,

    created_by   VARCHAR(255) NOT NULL,
    created_date TIMESTAMP    NOT NULL,

    updated_by   VARCHAR(255),
    updated_date TIMESTAMP,

    deleted_by   VARCHAR(255),
    deleted_date TIMESTAMP,

    CONSTRAINT pk_cities PRIMARY KEY (id),

    CONSTRAINT uk_cities_name_state
        UNIQUE (city_name, state_id),

    CONSTRAINT fk_cities_state
        FOREIGN KEY (state_id)
            REFERENCES master_data.states (id)
);

CREATE INDEX idx_cities_name
    ON master_data.cities (city_name);

CREATE INDEX idx_cities_state_id
    ON master_data.cities (state_id);

-- user Auth Roles and Permissions
CREATE TABLE user_auth.role
(
    id           BIGINT       NOT NULL DEFAULT nextval('user_auth.id_seq'),
    version      BIGINT       NOT NULL DEFAULT 0,

    role_id      BIGINT       NOT NULL UNIQUE,

    role_name    VARCHAR(20)  NOT NULL,
    description  VARCHAR(200),


    active_ind   BOOLEAN      NOT NULL DEFAULT TRUE,

    created_by   VARCHAR(255) NOT NULL,
    created_date TIMESTAMP    NOT NULL,

    updated_by   VARCHAR(255),
    updated_date TIMESTAMP,

    deleted_by   VARCHAR(255),
    deleted_date TIMESTAMP,

    CONSTRAINT pk_role PRIMARY KEY (id),

    CONSTRAINT uk_role_name
        UNIQUE (role_name)
);

CREATE INDEX idx_role_name
    ON user_auth.role (role_name);

CREATE TABLE user_auth.permission
(
    id              BIGINT       NOT NULL DEFAULT nextval('user_auth.id_seq'),
    version         BIGINT       NOT NULL DEFAULT 0,
    permission_id   BIGINT       NOT NULL UNIQUE,

    permission_name VARCHAR(50)  NOT NULL,

    active_ind      BOOLEAN      NOT NULL DEFAULT TRUE,

    created_by      VARCHAR(255) NOT NULL,
    created_date    TIMESTAMP    NOT NULL,

    updated_by      VARCHAR(255),
    updated_date    TIMESTAMP,

    deleted_by      VARCHAR(255),
    deleted_date    TIMESTAMP,

    CONSTRAINT pk_permission PRIMARY KEY (id),

    CONSTRAINT uk_permission_name
        UNIQUE (permission_name)
);

CREATE INDEX idx_permission_name
    ON user_auth.permission (permission_name);

CREATE TABLE user_auth.login_attempt
(
    id               BIGINT       NOT NULL DEFAULT nextval('user_auth.id_seq'),
    version          BIGINT       NOT NULL DEFAULT 0,
    username         VARCHAR(100) NOT NULL,
    ip_address       VARCHAR(45),
    user_agent       VARCHAR(500),
    failed_attempts  BIGINT       NOT NULL DEFAULT 0,
    account_blocked  BOOLEAN      NOT NULL DEFAULT FALSE,
    blocked_at       TIMESTAMP,
    block_expires_at TIMESTAMP,
    last_attempt_at  TIMESTAMP    NOT NULL,
    last_success_at  TIMESTAMP,
    active_ind       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by       VARCHAR(255) NOT NULL,
    created_date     TIMESTAMP    NOT NULL,
    updated_by       VARCHAR(255),
    updated_date     TIMESTAMP,
    deleted_by       VARCHAR(255),
    deleted_date     TIMESTAMP,
    CONSTRAINT pk_login_attempt PRIMARY KEY (id)
);

CREATE INDEX idx_login_attempt_username
    ON user_auth.login_attempt (username);

CREATE INDEX idx_login_attempt_ip_address
    ON user_auth.login_attempt (ip_address);

CREATE INDEX idx_login_attempt_blocked
    ON user_auth.login_attempt (account_blocked);

CREATE INDEX idx_login_attempt_last_attempt
    ON user_auth.login_attempt (last_attempt_at);

CREATE TABLE user_auth.role_permission
(
    role_id       BIGINT    NOT NULL,
    permission_id BIGINT    NOT NULL,
    assigned_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by   VARCHAR(255),
    CONSTRAINT pk_role_permission
        PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permission_role
        FOREIGN KEY (role_id)
            REFERENCES user_auth.role (id),
    CONSTRAINT fk_role_permission_permission
        FOREIGN KEY (permission_id)
            REFERENCES user_auth.permission (id)
);

CREATE TABLE user_auth.user_role
(
    user_id     BIGINT    NOT NULL,
    role_id     BIGINT    NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(255),
    CONSTRAINT pk_user_role
        PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id)
            REFERENCES user_auth.users (id),
    CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id)
            REFERENCES user_auth.role (id)
);

CREATE TABLE user_session.user_session
(
    id               BIGINT       NOT NULL DEFAULT nextval('user_auth.id_seq'),
    version          BIGINT       NOT NULL DEFAULT 0,

    user_id          BIGINT       NOT NULL,

    login_time       TIMESTAMP    NOT NULL,
    logout_time      TIMESTAMP,

    jwt_id           VARCHAR(255),
    refresh_token_id VARCHAR(255),

    ip_address       VARCHAR(45),
    user_agent       TEXT,
    device_info      VARCHAR(255),

    session_active   BOOLEAN      NOT NULL DEFAULT TRUE,

    created_by       VARCHAR(255) NOT NULL,
    created_date     TIMESTAMP    NOT NULL,

    updated_by       VARCHAR(255),
    updated_date     TIMESTAMP,

    deleted_by       VARCHAR(255),
    deleted_date     TIMESTAMP,

    CONSTRAINT pk_user_session PRIMARY KEY (id),

    CONSTRAINT fk_user_session_user
        FOREIGN KEY (user_id)
            REFERENCES user_auth.users (id)
);

CREATE INDEX idx_user_session_user_id
    ON user_session.user_session (user_id);

CREATE INDEX idx_user_session_active
    ON user_session.user_session (session_active);

CREATE INDEX idx_user_session_login_time
    ON user_session.user_session (login_time);

CREATE TABLE user_session.refresh_token
(
    id           BIGINT PRIMARY KEY    DEFAULT nextval('user_auth.id_seq'),

    user_id      BIGINT       NOT NULL,

    token_hash   VARCHAR(500) NOT NULL,

    expires_at   TIMESTAMP    NOT NULL,

    revoked      BOOLEAN      NOT NULL DEFAULT FALSE,

    revoked_at   TIMESTAMP,

    ip_address   VARCHAR(45),

    device_info  VARCHAR(255),

    created_date TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
            REFERENCES user_auth.users (id)
);



INSERT INTO master_data.countries (id,
                                   country_name,
                                   country_code,
                                   created_date,
                                   created_by)
VALUES (1,
        'India',
        'IN',
        NOW(),
        'System');

INSERT INTO master_data.states (version,
                                state_name,
                                state_code,
                                country_id,
                                active_ind,
                                created_by,
                                created_date)
VALUES (0, 'Andhra Pradesh', 'AP', 1, true, 'System', NOW()),
       (0, 'Arunachal Pradesh', 'AR', 1, true, 'System', NOW()),
       (0, 'Assam', 'AS', 1, true, 'System', NOW()),
       (0, 'Bihar', 'BR', 1, true, 'System', NOW()),
       (0, 'Chhattisgarh', 'CG', 1, true, 'System', NOW()),
       (0, 'Goa', 'GA', 1, true, 'System', NOW()),
       (0, 'Gujarat', 'GJ', 1, true, 'System', NOW()),
       (0, 'Haryana', 'HR', 1, true, 'System', NOW()),
       (0, 'Himachal Pradesh', 'HP', 1, true, 'System', NOW()),
       (0, 'Jharkhand', 'JH', 1, true, 'System', NOW()),
       (0, 'Karnataka', 'KA', 1, true, 'System', NOW()),
       (0, 'Kerala', 'KL', 1, true, 'System', NOW()),
       (0, 'Madhya Pradesh', 'MP', 1, true, 'System', NOW()),
       (0, 'Maharashtra', 'MH', 1, true, 'System', NOW()),
       (0, 'Manipur', 'MN', 1, true, 'System', NOW()),
       (0, 'Meghalaya', 'ML', 1, true, 'System', NOW()),
       (0, 'Mizoram', 'MZ', 1, true, 'System', NOW()),
       (0, 'Nagaland', 'NL', 1, true, 'System', NOW()),
       (0, 'Odisha', 'OD', 1, true, 'System', NOW()),
       (0, 'Punjab', 'PB', 1, true, 'System', NOW()),
       (0, 'Rajasthan', 'RJ', 1, true, 'System', NOW()),
       (0, 'Sikkim', 'SK', 1, true, 'System', NOW()),
       (0, 'Tamil Nadu', 'TN', 1, true, 'System', NOW()),
       (0, 'Telangana', 'TS', 1, true, 'System', NOW()),
       (0, 'Tripura', 'TR', 1, true, 'System', NOW()),
       (0, 'Uttar Pradesh', 'UP', 1, true, 'System', NOW()),
       (0, 'Uttarakhand', 'UK', 1, true, 'System', NOW()),
       (0, 'West Bengal', 'WB', 1, true, 'System', NOW()),
       (0, 'Andaman and Nicobar Islands', 'AN', 1, true, 'System', NOW()),
       (0, 'Chandigarh', 'CH', 1, true, 'System', NOW()),
       (0, 'Dadra and Nagar Haveli and Daman and Diu', 'DN', 1, true, 'System', NOW()),
       (0, 'Delhi', 'DL', 1, true, 'System', NOW()),
       (0, 'Jammu and Kashmir', 'JK', 1, true, 'System', NOW()),
       (0, 'Ladakh', 'LA', 1, true, 'System', NOW()),
       (0, 'Lakshadweep', 'LD', 1, true, 'System', NOW()),
       (0, 'Puducherry', 'PY', 1, true, 'System', NOW());

-- =========================
-- CITIES MASTER DATA
-- =========================

INSERT INTO master_data.cities (version,
                                city_name,
                                state_id,
                                active_ind,
                                created_by,
                                created_date)
VALUES (0, 'Visakhapatnam', 1, true, 'System', NOW()),
       (0, 'Itanagar', 2, true, 'System', NOW()),
       (0, 'Guwahati', 3, true, 'System', NOW()),
       (0, 'Patna', 4, true, 'System', NOW()),
       (0, 'Raipur', 5, true, 'System', NOW()),
       (0, 'Panaji', 6, true, 'System', NOW()),
       (0, 'Ahmedabad', 7, true, 'System', NOW()),
       (0, 'Gurugram', 8, true, 'System', NOW()),
       (0, 'Shimla', 9, true, 'System', NOW()),
       (0, 'Ranchi', 10, true, 'System', NOW()),
       (0, 'Bengaluru', 11, true, 'System', NOW()),
       (0, 'Kochi', 12, true, 'System', NOW()),
       (0, 'Bhopal', 13, true, 'System', NOW()),
       (0, 'Mumbai', 14, true, 'System', NOW()),
       (0, 'Imphal', 15, true, 'System', NOW()),
       (0, 'Shillong', 16, true, 'System', NOW()),
       (0, 'Aizawl', 17, true, 'System', NOW()),
       (0, 'Kohima', 18, true, 'System', NOW()),
       (0, 'Bhubaneswar', 19, true, 'System', NOW()),
       (0, 'Ludhiana', 20, true, 'System', NOW()),
       (0, 'Jaipur', 21, true, 'System', NOW()),
       (0, 'Gangtok', 22, true, 'System', NOW()),
       (0, 'Chennai', 23, true, 'System', NOW()),
       (0, 'Hyderabad', 24, true, 'System', NOW()),
       (0, 'Agartala', 25, true, 'System', NOW()),
       (0, 'Lucknow', 26, true, 'System', NOW()),
       (0, 'Noida', 26, true, 'System', NOW()),
       (0, 'Kanpur', 26, true, 'System', NOW()),
       (0, 'Dehradun', 27, true, 'System', NOW()),
       (0, 'Kolkata', 28, true, 'System', NOW()),
       (0, 'Port Blair', 29, true, 'System', NOW()),
       (0, 'Chandigarh', 30, true, 'System', NOW()),
       (0, 'Daman', 31, true, 'System', NOW()),
       (0, 'New Delhi', 32, true, 'System', NOW()),
       (0, 'Srinagar', 33, true, 'System', NOW()),
       (0, 'Leh', 34, true, 'System', NOW()),
       (0, 'Kavaratti', 35, true, 'System', NOW()),
       (0, 'Puducherry', 36, true, 'System', NOW())
ON CONFLICT (city_name, state_id)
    DO NOTHING;

