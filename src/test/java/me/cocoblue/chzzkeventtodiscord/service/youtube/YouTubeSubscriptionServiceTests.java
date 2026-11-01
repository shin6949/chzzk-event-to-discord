package me.cocoblue.chzzkeventtodiscord.service.youtube;

import me.cocoblue.chzzkeventtodiscord.data.LanguageIsoData;
import me.cocoblue.chzzkeventtodiscord.data.youtube.YouTubeSubscriptionType;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataRepository;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeChannelRepository;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeNotificationLogEntity;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeNotificationLogRepository;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeSubscriptionEntity;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeSubscriptionRepository;
import me.cocoblue.chzzkeventtodiscord.dto.youtube.YouTubeDtos;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:youtube_subscription_service_tests;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@Transactional
class YouTubeSubscriptionServiceTests {
    private static final String OWNER_CHANNEL_ID = "owner-channel";

    @Autowired
    private YouTubeSubscriptionService service;
    @Autowired
    private YouTubeSubscriptionRepository subscriptionRepository;
    @Autowired
    private YouTubeChannelRepository youtubeChannelRepository;
    @Autowired
    private YouTubeNotificationLogRepository notificationLogRepository;
    @Autowired
    private ChzzkChannelRepository chzzkChannelRepository;
    @Autowired
    private DiscordWebhookDataRepository webhookRepository;
    @Autowired
    private DiscordBotProfileDataRepository botProfileRepository;

    @MockBean
    private YouTubeApiService apiService;

    private ChzzkChannelEntity ownerChannel;
    private DiscordWebhookDataEntity webhook;
    private DiscordBotProfileDataEntity botProfile;

    @BeforeEach
    void setUp() {
        notificationLogRepository.deleteAll();
        subscriptionRepository.deleteAll();
        youtubeChannelRepository.deleteAll();
        botProfileRepository.deleteAll();
        webhookRepository.deleteAll();
        chzzkChannelRepository.deleteAll();

        ownerChannel = chzzkChannelRepository.saveAndFlush(createChannel(OWNER_CHANNEL_ID));
        webhook = webhookRepository.saveAndFlush(DiscordWebhookDataEntity.builder()
            .ownerId(ownerChannel)
            .name("Main webhook")
            .webhookUrl("https://discord.com/api/webhooks/test")
            .build());
        botProfile = botProfileRepository.saveAndFlush(DiscordBotProfileDataEntity.builder()
            .ownerId(ownerChannel)
            .alias("Main bot")
            .username("Notifier")
            .avatarUrl("bot-profiles/owner/avatar.png")
            .build());
    }

    @Test
    void createInitializesNewChannelWithCurrentLiveState() {
        when(apiService.getChannelState("UC123")).thenReturn(new YouTubeChannelState(
            new YouTubeChannelSnapshot("UC123", "Official Channel", "https://example.test/channel.jpg"),
            new YouTubeVideoSnapshot("live-video", "Live now", null, null, null),
            new YouTubeVideoSnapshot("latest-video", "Latest", null, null, null)
        ));

        final YouTubeSubscriptionEntity subscription = service.create(
            request("UC123", YouTubeSubscriptionType.LIVE_STARTED),
            new ChzzkPrincipal(OWNER_CHANNEL_ID, AppRole.USER)
        );

        final YouTubeChannelEntity channel = subscription.getYoutubeChannel();
        assertThat(channel.isCurrentlyLive()).isTrue();
        assertThat(channel.getCurrentLiveVideoId()).isEqualTo("live-video");
        assertThat(channel.getLastVideoId()).isEqualTo("latest-video");
    }

    @Test
    void deleteRemovesNotificationLogsBeforeSubscription() {
        final YouTubeChannelEntity youtubeChannel = youtubeChannelRepository.saveAndFlush(YouTubeChannelEntity.builder()
            .channelId("UC123")
            .title("Official Channel")
            .currentlyLive(false)
            .build());
        final YouTubeSubscriptionEntity subscription = subscriptionRepository.saveAndFlush(YouTubeSubscriptionEntity.builder()
            .youtubeChannel(youtubeChannel)
            .type(YouTubeSubscriptionType.LIVE_STARTED)
            .webhook(webhook)
            .formOwner(ownerChannel)
            .languageIsoData(LanguageIsoData.Korean)
            .intervalMinute(10)
            .enabled(true)
            .botProfile(botProfile)
            .colorHex("ff0000")
            .build());
        notificationLogRepository.saveAndFlush(YouTubeNotificationLogEntity.builder()
            .subscription(subscription)
            .build());

        service.delete(subscription.getId(), new ChzzkPrincipal(OWNER_CHANNEL_ID, AppRole.USER));

        assertThat(subscriptionRepository.findById(subscription.getId())).isEmpty();
        assertThat(notificationLogRepository.count()).isZero();
    }

    private YouTubeDtos.SubscriptionRequest request(String channelId, YouTubeSubscriptionType type) {
        final YouTubeDtos.SubscriptionRequest request = new YouTubeDtos.SubscriptionRequest();
        request.setYoutubeChannelId(channelId);
        request.setType(type);
        request.setWebhookId(webhook.getId());
        request.setBotProfileId(botProfile.getId());
        return request;
    }

    private ChzzkChannelEntity createChannel(String channelId) {
        return ChzzkChannelEntity.builder()
            .channelId(channelId)
            .channelName(channelId)
            .profileUrl("https://example.test/profile/" + channelId)
            .isVerifiedMark(false)
            .channelDescription("desc-" + channelId)
            .subscriptionAvailability(true)
            .isLive(false)
            .followerCount(0)
            .lastCheckTime(ZonedDateTime.now(ZoneId.of("UTC")))
            .build();
    }
}
