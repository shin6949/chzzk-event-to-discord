package me.cocoblue.chzzkeventtodiscord.domain.youtube;

import me.cocoblue.chzzkeventtodiscord.data.youtube.YouTubeSubscriptionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface YouTubeSubscriptionRepository extends JpaRepository<YouTubeSubscriptionEntity, Long> {
    Page<YouTubeSubscriptionEntity> findAllByFormOwner_ChannelId(String channelId, Pageable pageable);

    List<YouTubeSubscriptionEntity> findAllByEnabled(boolean enabled);

    List<YouTubeSubscriptionEntity> findAllByYoutubeChannel_ChannelIdAndTypeAndEnabled(String channelId, YouTubeSubscriptionType type, boolean enabled);
}
