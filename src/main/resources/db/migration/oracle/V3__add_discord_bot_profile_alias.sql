ALTER TABLE discord_bot_profile_data ADD (alias VARCHAR2(500 CHAR));
UPDATE discord_bot_profile_data SET alias = username WHERE alias IS NULL;
ALTER TABLE discord_bot_profile_data MODIFY (alias VARCHAR2(500 CHAR) NOT NULL);
