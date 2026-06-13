package me.cocoblue.chzzkeventtodiscord.service.youtube;

import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeChannelEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class YouTubeEventClassifierTests {
    private final YouTubeEventClassifier classifier = new YouTubeEventClassifier();

    @Test
    void detectsLiveStartedLiveEndedAndNewVideo() {
        YouTubeChannelEntity before = YouTubeChannelEntity.builder()
            .channelId("UC123")
            .title("Official Channel")
            .currentlyLive(false)
            .lastVideoId("old-video")
            .build();
        YouTubeChannelState liveState = new YouTubeChannelState(
            new YouTubeChannelSnapshot("UC123", "Official Channel", "https://example.test/channel.jpg"),
            new YouTubeVideoSnapshot("live-video", "Live", null, null, null),
            new YouTubeVideoSnapshot("new-video", "New", null, null, null)
        );

        assertThat(classifier.isLiveStarted(before, liveState)).isTrue();
        assertThat(classifier.isNewVideoUploaded(before, liveState)).isTrue();

        before.setCurrentlyLive(true);
        YouTubeChannelState offlineState = new YouTubeChannelState(liveState.channel(), null, liveState.latestVideo());
        assertThat(classifier.isLiveEnded(before, offlineState)).isTrue();
    }

    @Test
    void doesNotSendVideoUploadForInitialSnapshot() {
        YouTubeChannelEntity before = YouTubeChannelEntity.builder()
            .channelId("UC123")
            .title("Official Channel")
            .currentlyLive(false)
            .lastVideoId(null)
            .build();
        YouTubeChannelState after = new YouTubeChannelState(
            new YouTubeChannelSnapshot("UC123", "Official Channel", null),
            null,
            new YouTubeVideoSnapshot("first-video", "First", null, null, null)
        );

        assertThat(classifier.isNewVideoUploaded(before, after)).isFalse();
    }
}
