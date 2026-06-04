ALTER TABLE discord_bot_profile_data ADD COLUMN alias VARCHAR(500);
UPDATE discord_bot_profile_data SET alias = username WHERE alias IS NULL;
ALTER TABLE discord_bot_profile_data ALTER COLUMN alias SET NOT NULL;
