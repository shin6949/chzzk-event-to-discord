DROP TABLE IF EXISTS notification_log;
DROP TABLE IF EXISTS chzzk_subscription_form;
DROP TABLE IF EXISTS discord_bot_profile_data;
DROP TABLE IF EXISTS chzzk_live_status;
DROP TABLE IF EXISTS chzzk_category;
DROP TABLE IF EXISTS discord_webhook_data;
DROP TABLE IF EXISTS chzzk_channel;
DROP TABLE IF EXISTS chzzk_subscription_stream_online_form;

create table chzzk_channel (
   channel_id varchar(255) not null primary key,
   channel_name varchar(33) not null,
   channel_description varchar(550),
   profile_url varchar(32780),
   follower_count integer not null,
   is_live boolean not null,
   subscription_availability BOOLEAN DEFAULT FALSE,
   is_verified_mark boolean not null,
   last_check_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP not null
);

create table discord_webhook_data (
  id bigserial not null primary key,
  name varchar(500) not null,
  webhook_url varchar(500) not null,
  meno TEXT,
  owner_id varchar(255) not null,
  CONSTRAINT FK_WEBHOOK_DATA_OWNER_ID FOREIGN KEY (owner_id) REFERENCES chzzk_channel
);

create table chzzk_category (
    category_type varchar(255) not null,
    category_id varchar(255) not null,
    category_name varchar(255) not null,
    -- NULLABLE added at Ver.0.1.2
    poster_image_url varchar(32780),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP not null,
    primary key (category_id, category_type)
);

create table discord_bot_profile_data (
    id bigserial not null primary key,
    username varchar(100) not null,
    avatar_url varchar(600) not null,
    owner_id varchar(255) not null,
    CONSTRAINT FK_BOT_PROFILE_DATA_OWNER_ID FOREIGN KEY (owner_id) REFERENCES chzzk_channel
);

CREATE TABLE chzzk_subscription_form (
    id bigserial NOT NULL PRIMARY KEY,
    enabled boolean NOT NULL DEFAULT true,
    interval_minute INTEGER DEFAULT 10 NOT NULL,
    bot_profile_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    webhook_id BIGINT NOT NULL,
    color_hex VARCHAR(11) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    channel_id VARCHAR(255),
    form_owner VARCHAR(255) NOT NULL,
    language VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    CONSTRAINT FK_CHZZK_SUBSCRIPTION_FORM_BOT_PROFILE_ID FOREIGN KEY (bot_profile_id) REFERENCES discord_bot_profile_data,
    CONSTRAINT FK_CHZZK_SUBSCRIPTION_FORM_CHANNEL_ID FOREIGN KEY (channel_id) REFERENCES chzzk_channel,
    CONSTRAINT FK_CHZZK_SUBSCRIPTION_FORM_OWNER_CHANNEL_ID FOREIGN KEY (form_owner) REFERENCES chzzk_channel,
    CONSTRAINT FK_CHZZK_SUBSCRIPTION_FORM_WEBHOOK_ID FOREIGN KEY (webhook_id) REFERENCES discord_webhook_data
);

create table notification_log (
    log_id bigserial not null primary key,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP not null,
    form_id BIGINT not null,
    CONSTRAINT FK_NOTIFICATION_LOG_FORM_ID FOREIGN KEY (form_id) REFERENCES chzzk_subscription_form(id)
);

-- @since 0.1.3
CREATE TABLE chzzk_subscription_stream_online_form (
    id  bigserial   PRIMARY KEY,
    show_detail  BOOLEAN  NOT NULL DEFAULT 0,
    show_thumbnail BOOLEAN  NOT NULL DEFAULT 1,
    FOREIGN KEY (id) REFERENCES chzzk_subscription_form (id) ON DELETE CASCADE
);
