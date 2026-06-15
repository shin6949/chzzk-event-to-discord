package me.cocoblue.chzzkeventtodiscord.domain.twitch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TwitchEventSubscriptionRepository extends JpaRepository<TwitchEventSubscriptionEntity, Long> {
    List<TwitchEventSubscriptionEntity> findAllByOwnerChannelIdOrderByCreatedAtDesc(String ownerChannelId);

    Optional<TwitchEventSubscriptionEntity> findByEventsubOnlineIdAndEnabledTrue(String eventsubOnlineId);

    Optional<TwitchEventSubscriptionEntity> findByEventsubOfflineIdAndEnabledTrue(String eventsubOfflineId);
}
