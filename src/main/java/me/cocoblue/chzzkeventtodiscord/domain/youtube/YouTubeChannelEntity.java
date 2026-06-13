package me.cocoblue.chzzkeventtodiscord.domain.youtube;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "youtube_channel")
public class YouTubeChannelEntity {
    @Id
    @Column(name = "channel_id", nullable = false, length = 64)
    private String channelId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    @Column(name = "currently_live", nullable = false)
    private boolean currentlyLive;

    @Column(name = "current_live_video_id", length = 64)
    private String currentLiveVideoId;

    @Column(name = "last_video_id", length = 64)
    private String lastVideoId;

    @UpdateTimestamp
    @Column(name = "last_checked_at")
    private ZonedDateTime lastCheckedAt;
}
