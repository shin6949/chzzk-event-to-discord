package me.cocoblue.chzzkeventtodiscord.domain.youtube;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface YouTubeNotificationLogRepository extends JpaRepository<YouTubeNotificationLogEntity, Long> {
    List<YouTubeNotificationLogEntity> findAllBySubscriptionAndCreatedAtBetween(
        YouTubeSubscriptionEntity subscription,
        ZonedDateTime start,
        ZonedDateTime end
    );
}
