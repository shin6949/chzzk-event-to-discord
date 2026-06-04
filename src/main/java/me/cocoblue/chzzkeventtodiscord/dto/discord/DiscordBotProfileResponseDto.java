package me.cocoblue.chzzkeventtodiscord.dto.discord;

import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataEntity;
import me.cocoblue.chzzkeventtodiscord.service.StaticContentUrlResolver;

public record DiscordBotProfileResponseDto(
    Long id,
    String alias,
    String username,
    String avatarUrl,
    String ownerChannelId
) {
    public static DiscordBotProfileResponseDto fromEntity(
        DiscordBotProfileDataEntity entity,
        StaticContentUrlResolver staticContentUrlResolver
    ) {
        return new DiscordBotProfileResponseDto(
            entity.getId(),
            entity.getAlias(),
            entity.getUsername(),
            staticContentUrlResolver.resolve(entity.getAvatarUrl()),
            entity.getOwnerId().getChannelId()
        );
    }
}
