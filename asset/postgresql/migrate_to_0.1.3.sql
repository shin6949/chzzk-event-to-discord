DROP TABLE IF EXISTS chzzk_subscription_stream_online_form;

CREATE TABLE chzzk_subscription_stream_online_form (
    id  bigserial   PRIMARY KEY,
    show_detail  BOOLEAN  NOT NULL DEFAULT false,
    show_thumbnail BOOLEAN  NOT NULL DEFAULT true,
    FOREIGN KEY (id) REFERENCES chzzk_subscription_form (id) ON DELETE CASCADE
);

INSERT INTO chzzk_subscription_stream_online_form (id, show_detail)
SELECT id, show_detail FROM chzzk_subscription_form WHERE type LIKE 'STREAM_ONLINE';

ALTER TABLE chzzk_subscription_form DROP COLUMN show_detail;

ALTER TABLE discord_webhook_data ALTER COLUMN webhook_url TYPE VARCHAR(30000);
ALTER TABLE chzzk_category ALTER COLUMN updated_at DROP DEFAULT;
ALTER TABLE chzzk_channel ALTER COLUMN profile_url TYPE VARCHAR(30000);
ALTER TABLE discord_bot_profile_data ALTER COLUMN avatar_url TYPE VARCHAR(30000);
ALTER TABLE chzzk_subscription_form ALTER COLUMN color_hex SET DEFAULT '000000';