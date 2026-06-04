package me.cocoblue.chzzkeventtodiscord.dto.discord;

import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;

public record DiscordWebhookResponseDto(
    Long id,
    String alias,
    String url,
    String ownerChannelId
) {
    public static DiscordWebhookResponseDto fromEntity(DiscordWebhookDataEntity entity) {
        return new DiscordWebhookResponseDto(
            entity.getId(),
            entity.getName(),
            entity.getWebhookUrl(),
            entity.getOwnerId().getChannelId()
        );
    }
}
