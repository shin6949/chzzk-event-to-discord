package me.cocoblue.chzzkeventtodiscord.service.youtube;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.data.youtube.YouTubeSubscriptionType;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeNotificationLogEntity;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeNotificationLogRepository;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeSubscriptionEntity;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeSubscriptionRepository;
import me.cocoblue.chzzkeventtodiscord.dto.discord.DiscordEmbed;
import me.cocoblue.chzzkeventtodiscord.service.DiscordWebhookService;
import me.cocoblue.chzzkeventtodiscord.service.StaticContentUrlResolver;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class YouTubeEventSender {
    private static final String YOUTUBE_ICON_URL = "https://www.youtube.com/s/desktop/6f2b4b21/img/favicon_144x144.png";

    private final YouTubeSubscriptionRepository subscriptionRepository;
    private final YouTubeNotificationLogRepository notificationLogRepository;
    private final DiscordWebhookService discordWebhookService;
    private final StaticContentUrlResolver staticContentUrlResolver;

    @Async
    public void sendEvent(String channelId, YouTubeSubscriptionType type, YouTubeChannelState state) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        List<YouTubeSubscriptionEntity> forms = subscriptionRepository
            .findAllByYoutubeChannel_ChannelIdAndTypeAndEnabled(channelId, type, true)
            .stream()
            .filter(form -> notificationLogRepository.findAllBySubscriptionAndCreatedAtBetween(
                form,
                now.minusMinutes(form.getIntervalMinute()),
                now
            ).isEmpty())
            .toList();

        forms.forEach(form -> send(form, type, state));
    }

    private void send(YouTubeSubscriptionEntity form, YouTubeSubscriptionType type, YouTubeChannelState state) {
        DiscordEmbed.Webhook webhook = makeWebhook(form, type, state);
        discordWebhookService.sendDiscordWebhook(webhook, form.getWebhook().getWebhookUrl());
        notificationLogRepository.save(YouTubeNotificationLogEntity.builder().subscription(form).build());
    }

    DiscordEmbed.Webhook makeWebhook(YouTubeSubscriptionEntity form, YouTubeSubscriptionType type, YouTubeChannelState state) {
        YouTubeVideoSnapshot video = type == YouTubeSubscriptionType.LIVE_ENDED ? null
            : (type == YouTubeSubscriptionType.LIVE_STARTED ? state.liveVideo() : state.latestVideo());
        String channelUrl = "https://www.youtube.com/channel/" + state.channel().channelId();
        String eventText = switch (type) {
            case LIVE_STARTED -> "라이브 스트리밍을 시작했습니다";
            case LIVE_ENDED -> "라이브 스트리밍을 종료했습니다";
            case VIDEO_UPLOADED -> "새 영상을 업로드했습니다";
        };
        String url = video == null ? channelUrl : "https://www.youtube.com/watch?v=" + video.videoId();
        String title = video == null ? state.channel().title() : video.title();
        DiscordEmbed.Author author = new DiscordEmbed.Author(state.channel().title() + "님이 " + eventText, channelUrl, state.channel().thumbnailUrl());
        DiscordEmbed.Footer footer = new DiscordEmbed.Footer("YouTube", YOUTUBE_ICON_URL);
        DiscordEmbed.Image image = video == null || video.thumbnailUrl() == null ? null : DiscordEmbed.Image.builder().url(video.thumbnailUrl()).build();
        DiscordEmbed embed = DiscordEmbed.builder()
            .author(author)
            .title(title)
            .url(url)
            .description(eventText)
            .color(Integer.toString(form.getDecimalColor()))
            .footer(footer)
            .timestamp(ZonedDateTime.now(ZoneId.of("UTC")).toString())
            .image(image)
            .build();
        return new DiscordEmbed.Webhook(
            form.getBotProfile().getUsername(),
            staticContentUrlResolver.resolve(form.getBotProfile().getAvatarUrl()),
            form.getContent(),
            List.of(embed)
        );
    }
}
