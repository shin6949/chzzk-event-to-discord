package me.cocoblue.chzzkeventtodiscord.dto.twitch;

import lombok.Data;

@Data
public class TwitchSubscriptionRequestDto {
    private String broadcasterLogin;
    private String broadcasterUserId;
    private String discordWebhookUrl;
    private Boolean enabled;
}
