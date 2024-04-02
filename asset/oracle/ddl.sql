-- This DDL works since 1.0.3

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE notification_log';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE chzzk_subscription_stream_online_form';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE chzzk_subscription_form';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE discord_bot_profile_data';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE discord_webhook_data';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE chzzk_category';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE chzzk_channel';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN
            RAISE;
        END IF;
END;
/

CREATE TABLE discord_bot_profile_data (
    id                  DECIMAL(38)     GENERATED AS IDENTITY PRIMARY KEY,
    owner_id            VARCHAR2(255)   NOT NULL,
    username            VARCHAR2(100)   NOT NULL,
    avatar_url          VARCHAR2(30000) NOT NULL
);

CREATE TABLE discord_webhook_data (
    id                  DECIMAL(38)     GENERATED AS IDENTITY PRIMARY KEY,
    name                VARCHAR2(500)   NOT NULL,
    webhook_url         VARCHAR2(30000) NOT NULL,
    meno                VARCHAR2(500),
    owner_id            VARCHAR2(255)   NOT NULL
);

CREATE TABLE chzzk_category (
    category_id         VARCHAR2(255)   NOT NULL,
    category_type       VARCHAR2(255)   NOT NULL,
    category_name       VARCHAR2(255)   NOT NULL,
    poster_image_url    VARCHAR2(30000) NOT NULL,
    updated_at          TIMESTAMP       WITH TIME ZONE NOT NULL,
    PRIMARY KEY (category_id, category_type)
);

CREATE TABLE chzzk_channel (
    channel_id                  VARCHAR2(255)   NOT NULL PRIMARY KEY,
    channel_name                VARCHAR2(255)   NOT NULL,
    profile_url                 VARCHAR2(30000) NOT NULL,
    is_verified_mark            NUMBER(1, 0)    NOT NULL,
    channel_description         VARCHAR2(550),
    subscription_availability   NUMBER(1, 0)    NOT NULL,
    is_live                     NUMBER(1, 0)    NOT NULL,
    follower_count              INTEGER         DEFAULT 0 NOT NULL,
    last_check_time             TIMESTAMP       WITH TIME ZONE NOT NULL
);

CREATE TABLE chzzk_subscription_form (
    id              DECIMAL(38)     GENERATED AS IDENTITY PRIMARY KEY,
    channel_id      VARCHAR2(255 CHAR)   NOT NULL,
    type            VARCHAR2(255 CHAR)   NOT NULL,
    webhook_id      DECIMAL(38)          NOT NULL,
    form_owner      VARCHAR2(255 CHAR)   NOT NULL,
    language        VARCHAR2(255 CHAR)   NOT NULL,
    created_at      TIMESTAMP       WITH TIME ZONE NOT NULL,
    interval_minute INTEGER         DEFAULT 10 NOT NULL,
    enabled         NUMBER(1, 0)    NOT NULL,
    bot_profile_id  DECIMAL(38)     NOT NULL,
    content         VARCHAR2(2000 CHAR),
    color_hex       VARCHAR2(11 CHAR)    DEFAULT '000000' NOT NULL
);

CREATE TABLE chzzk_subscription_stream_online_form (
    id              DECIMAL(38) NOT NULL PRIMARY KEY,
    show_detail     NUMBER(1)   DEFAULT 0 NOT NULL,
    show_thumbnail  NUMBER(1)   DEFAULT 1 NOT NULL
);

CREATE TABLE notification_log (
    log_id      DECIMAL(38)     GENERATED AS IDENTITY PRIMARY KEY NOT NULL,
    form_id     DECIMAL(38)     NOT NULL,
    created_at  TIMESTAMP       WITH TIME ZONE NOT NULL
);

-- discord_webhook_data FK
ALTER TABLE discord_webhook_data ADD CONSTRAINT fk_discord_webhook_data_owner_id FOREIGN KEY (owner_id) REFERENCES chzzk_channel(channel_id);

-- discord_bot_profile_data FK
ALTER TABLE discord_bot_profile_data ADD CONSTRAINT fk_bot_profile_data_owner_id FOREIGN KEY (owner_id) REFERENCES chzzk_channel(channel_id);

-- chzzk_subscription_form FK
ALTER TABLE chzzk_subscription_form ADD CONSTRAINT fk_chzzk_subscription_form_channel_id FOREIGN KEY (channel_id) REFERENCES chzzk_channel(channel_id);
ALTER TABLE chzzk_subscription_form ADD CONSTRAINT fk_chzzk_subscription_form_webhook_id FOREIGN KEY (webhook_id) REFERENCES discord_webhook_data(id);
ALTER TABLE chzzk_subscription_form ADD CONSTRAINT fk_chzzk_subscription_form_owner_channel_id FOREIGN KEY (form_owner) REFERENCES chzzk_channel(channel_id);
ALTER TABLE chzzk_subscription_form ADD CONSTRAINT fk_chzzk_subscription_form_bot_profile_id FOREIGN KEY (bot_profile_id) REFERENCES discord_bot_profile_data(id);

-- chzzk_subscription_stream_online_form FK
ALTER TABLE chzzk_subscription_stream_online_form ADD CONSTRAINT fk_chzzk_subscription_stream_online_form_id FOREIGN KEY (id) REFERENCES chzzk_subscription_form(id) ON DELETE CASCADE;

-- notification_log FK
ALTER TABLE notification_log ADD CONSTRAINT fk_notification_log_form_id FOREIGN KEY (form_id) REFERENCES chzzk_subscription_form(id) ON DELETE CASCADE;