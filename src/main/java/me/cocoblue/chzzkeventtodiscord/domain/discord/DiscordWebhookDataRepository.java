package me.cocoblue.chzzkeventtodiscord.domain.discord;

import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscordWebhookDataRepository extends JpaRepository<DiscordWebhookDataEntity, Long> {
    Optional<DiscordWebhookDataEntity> findDiscordWebhookDataEntityByWebhookUrlAndNameAndOwnerId(String webhookUrl, String name,
                                                                                                 ChzzkChannelEntity ownerId);

    Page<DiscordWebhookDataEntity> findAllByOwnerId_ChannelId(String channelId, Pageable pageable);

    Optional<DiscordWebhookDataEntity> findByIdAndOwnerId_ChannelId(Long id, String channelId);

    boolean existsByNameAndOwnerId_ChannelId(String name, String channelId);
}
