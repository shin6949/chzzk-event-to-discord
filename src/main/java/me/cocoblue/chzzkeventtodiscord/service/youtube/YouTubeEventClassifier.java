package me.cocoblue.chzzkeventtodiscord.service.youtube;

import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeChannelEntity;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class YouTubeEventClassifier {
    public boolean isLiveStarted(YouTubeChannelEntity before, YouTubeChannelState after) {
        return !before.isCurrentlyLive() && after.live();
    }

    public boolean isLiveEnded(YouTubeChannelEntity before, YouTubeChannelState after) {
        return before.isCurrentlyLive() && !after.live();
    }

    public boolean isNewVideoUploaded(YouTubeChannelEntity before, YouTubeChannelState after) {
        return after.latestVideo() != null
            && before.getLastVideoId() != null
            && !Objects.equals(before.getLastVideoId(), after.latestVideo().videoId());
    }
}
