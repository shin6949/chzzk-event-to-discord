package me.cocoblue.chzzkeventtodiscord.dto.discord;

import lombok.Data;

@Data
public class DiscordWebhookRequestDto {
    private String alias;
    private String url;
    private String ownerChannelId;
}
