package me.cocoblue.chzzkeventtodiscord.domain.discord;

import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscordWebhookDataRepository extends JpaRepository<DiscordWebhookDataEntity, Long> {
    Optional<DiscordWebhookDataEntity> findDiscordWebhookDataEntityByWebhookUrlAndNameAndOwnerId(String webhookUrl, String name,
                                                                                                 ChzzkChannelEntity ownerId);
}