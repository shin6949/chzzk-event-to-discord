package me.cocoblue.chzzkeventtodiscord.service.youtube;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.data.youtube.YouTubeSubscriptionType;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeChannelRepository;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeSubscriptionEntity;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeSubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class YouTubeEventReader {
    private final YouTubeSubscriptionRepository subscriptionRepository;
    private final YouTubeChannelRepository channelRepository;
    private final YouTubeApiService apiService;
    private final YouTubeEventClassifier classifier;
    private final YouTubeEventSender sender;

    @Value("${app.is-test:false}")
    private boolean isTest;

    @Scheduled(fixedRateString = "#{${youtube.check-interval:60} * 1000}")
    public void readEvent() {
        if (isTest) {
            log.info("Test mode is enabled. Skip YouTube scheduled task.");
            return;
        }
        Set<String> channelIds = subscriptionRepository.findAllByEnabled(true).stream()
            .map(YouTubeSubscriptionEntity::getYoutubeChannel)
            .map(YouTubeChannelEntity::getChannelId)
            .collect(Collectors.toSet());
        channelIds.forEach(this::classifyEventAndRunTrigger);
    }

    public void classifyEventAndRunTrigger(String channelId) {
        YouTubeChannelEntity before = channelRepository.findById(channelId).orElseThrow();
        YouTubeChannelState after = apiService.getChannelState(channelId);

        if (classifier.isLiveStarted(before, after)) {
            sender.sendEvent(channelId, YouTubeSubscriptionType.LIVE_STARTED, after);
        } else if (classifier.isLiveEnded(before, after)) {
            sender.sendEvent(channelId, YouTubeSubscriptionType.LIVE_ENDED, after);
        }
        if (classifier.isNewVideoUploaded(before, after)) {
            sender.sendEvent(channelId, YouTubeSubscriptionType.VIDEO_UPLOADED, after);
        }

        before.setTitle(after.channel().title());
        before.setThumbnailUrl(after.channel().thumbnailUrl());
        before.setCurrentlyLive(after.live());
        before.setCurrentLiveVideoId(after.liveVideo() == null ? null : after.liveVideo().videoId());
        before.setLastVideoId(after.latestVideo() == null ? before.getLastVideoId() : after.latestVideo().videoId());
        channelRepository.save(before);
    }
}
