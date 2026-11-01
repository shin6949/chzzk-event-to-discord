package me.cocoblue.chzzkeventtodiscord.service.soop;

import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataRepository;
import me.cocoblue.chzzkeventtodiscord.domain.soop.SoopSubscriptionEntity;
import me.cocoblue.chzzkeventtodiscord.domain.soop.SoopSubscriptionRepository;
import me.cocoblue.chzzkeventtodiscord.dto.discord.DiscordEmbed;
import me.cocoblue.chzzkeventtodiscord.dto.soop.SoopLiveStatusDto;
import me.cocoblue.chzzkeventtodiscord.service.DiscordWebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.static-content.url-prefix=https://static.example.test/assets",
    "spring.datasource.url=jdbc:h2:mem:soop_subscription_event_processor_tests;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
@Transactional
class SoopSubscriptionEventProcessorTests {
    private static final String OWNER_CHANNEL_ID = "owner-channel";

    @Autowired
    private SoopSubscriptionEventProcessor processor;
    @Autowired
    private SoopSubscriptionRepository soopSubscriptionRepository;
    @Autowired
    private ChzzkChannelRepository chzzkChannelRepository;
    @Autowired
    private DiscordWebhookDataRepository webhookRepository;
    @Autowired
    private DiscordBotProfileDataRepository botProfileRepository;

    @MockBean
    private SoopLiveStatusService soopLiveStatusService;
    @MockBean
    private DiscordWebhookService discordWebhookService;

    private ChzzkChannelEntity ownerChannel;
    private DiscordWebhookDataEntity webhook;
    private DiscordBotProfileDataEntity botProfile;

    @BeforeEach
    void setUp() {
        soopSubscriptionRepository.deleteAll();
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
    void processorPersistsStatusUpdateAndResolvesManagedAvatarUrl() {
        final SoopSubscriptionEntity subscription = soopSubscriptionRepository.saveAndFlush(SoopSubscriptionEntity.builder()
            .soopUserId("soop123")
            .soopChannelName("soop123")
            .live(false)
            .enabled(true)
            .notifyOnline(true)
            .notifyOffline(true)
            .content("hello")
            .colorHex("9146FF")
            .webhook(webhook)
            .botProfile(botProfile)
            .formOwner(ownerChannel)
            .build());
        when(soopLiveStatusService.getLiveStatus("soop123")).thenReturn(new SoopLiveStatusDto(
            "soop123",
            "Streamer",
            "https://profile.example.test/logo.jpg",
            true,
            "2232",
            "테스트 방송",
            null,
            null,
            "42",
            "https://play.sooplive.co.kr/soop123/2232"
        ));

        processor.classifyAndSend(subscription.getId());
        processor.classifyAndSend(subscription.getId());

        final SoopSubscriptionEntity saved = soopSubscriptionRepository.findById(subscription.getId()).orElseThrow();
        assertThat(saved.isLive()).isTrue();
        assertThat(saved.getSoopChannelName()).isEqualTo("Streamer");
        assertThat(saved.getLiveUrl()).isEqualTo("https://play.sooplive.co.kr/soop123/2232");

        final ArgumentCaptor<DiscordEmbed.Webhook> webhookCaptor = ArgumentCaptor.forClass(DiscordEmbed.Webhook.class);
        verify(discordWebhookService, times(1))
            .sendDiscordWebhook(webhookCaptor.capture(), eq("https://discord.com/api/webhooks/test"));
        assertThat(webhookCaptor.getValue().getAvatarUrl())
            .isEqualTo("https://static.example.test/assets/bot-profiles/owner/avatar.png");
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
