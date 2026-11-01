package me.cocoblue.chzzkeventtodiscord.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.cocoblue.chzzkeventtodiscord.data.LanguageIsoData;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkSubscriptionType;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataRepository;
import me.cocoblue.chzzkeventtodiscord.domain.soop.SoopSubscriptionEntity;
import me.cocoblue.chzzkeventtodiscord.domain.soop.SoopSubscriptionRepository;
import me.cocoblue.chzzkeventtodiscord.service.BotProfileImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.static-content.url-prefix=https://static.example.test/assets")
@Transactional
class DiscordResourceControllerTests {
    private static final String OWNER_CHANNEL_ID = "owner-channel";
    private static final String OTHER_CHANNEL_ID = "other-channel";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ChzzkChannelRepository chzzkChannelRepository;
    @Autowired
    private DiscordWebhookDataRepository discordWebhookDataRepository;
    @Autowired
    private DiscordBotProfileDataRepository discordBotProfileDataRepository;
    @Autowired
    private ChzzkSubscriptionFormRepository subscriptionFormRepository;
    @Autowired
    private SoopSubscriptionRepository soopSubscriptionRepository;

    @MockBean
    private BotProfileImageStorageService botProfileImageStorageService;

    private ChzzkChannelEntity ownerChannel;
    private ChzzkChannelEntity otherChannel;

    @BeforeEach
    void setUp() {
        soopSubscriptionRepository.deleteAll();
        subscriptionFormRepository.deleteAll();
        discordBotProfileDataRepository.deleteAll();
        discordWebhookDataRepository.deleteAll();
        chzzkChannelRepository.deleteAll();

        ownerChannel = createChannel(OWNER_CHANNEL_ID, "Owner");
        otherChannel = createChannel(OTHER_CHANNEL_ID, "Other");
    }

    @Test
    void userCanCreateAndListOwnedWebhooks() throws Exception {
        discordWebhookDataRepository.save(DiscordWebhookDataEntity.builder()
            .ownerId(otherChannel)
            .name("Other webhook")
            .webhookUrl("https://example.test/webhook/other")
            .build());

        final String payload = objectMapper.writeValueAsString(Map.of(
            "alias", "Main webhook",
            "url", "https://example.test/webhook/main"
        ));

        mockMvc.perform(post("/api/v1/discord/webhooks")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.alias").value("Main webhook"))
            .andExpect(jsonPath("$.url").value("https://example.test/webhook/main"))
            .andExpect(jsonPath("$.ownerChannelId").value(OWNER_CHANNEL_ID));

        mockMvc.perform(get("/api/v1/discord/webhooks")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.number").value(0))
            .andExpect(jsonPath("$.size").value(20))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.pageable").doesNotExist())
            .andExpect(jsonPath("$.sort").doesNotExist())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].alias").value("Main webhook"));
    }

    @Test
    void userCanUpdateOwnedWebhook() throws Exception {
        final DiscordWebhookDataEntity webhook = discordWebhookDataRepository.save(DiscordWebhookDataEntity.builder()
            .ownerId(ownerChannel)
            .name("Original webhook")
            .webhookUrl("https://example.test/webhook/original")
            .build());

        final String payload = objectMapper.writeValueAsString(Map.of(
            "alias", "Edited webhook",
            "url", "https://example.test/webhook/edited"
        ));

        mockMvc.perform(put("/api/v1/discord/webhooks/{id}", webhook.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(webhook.getId()))
            .andExpect(jsonPath("$.alias").value("Edited webhook"))
            .andExpect(jsonPath("$.url").value("https://example.test/webhook/edited"));
    }

    @Test
    void botProfileUploadStoresObjectKeyAndReturnsResolvedUrl() throws Exception {
        when(botProfileImageStorageService.upload(eq(OWNER_CHANNEL_ID), any(MultipartFile.class)))
            .thenReturn("bot-profiles/owner-channel/avatar.png");

        final MockMultipartFile avatar = new MockMultipartFile(
            "avatar",
            "avatar.png",
            "image/png",
            "avatar".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/discord/bot-profiles")
                .file(avatar)
                .param("alias", "Main bot")
                .param("username", "Notifier")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.alias").value("Main bot"))
            .andExpect(jsonPath("$.username").value("Notifier"))
            .andExpect(jsonPath("$.avatarUrl").value("https://static.example.test/assets/bot-profiles/owner-channel/avatar.png"));

        mockMvc.perform(get("/api/v1/discord/bot-profiles")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.number").value(0))
            .andExpect(jsonPath("$.size").value(20))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.pageable").doesNotExist())
            .andExpect(jsonPath("$.sort").doesNotExist())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].avatarUrl").value("https://static.example.test/assets/bot-profiles/owner-channel/avatar.png"));
    }

    @Test
    void userCannotDeleteResourcesReferencedBySubscription() throws Exception {
        final DiscordWebhookDataEntity webhook = discordWebhookDataRepository.save(DiscordWebhookDataEntity.builder()
            .ownerId(ownerChannel)
            .name("Main webhook")
            .webhookUrl("https://example.test/webhook/main")
            .build());
        final DiscordBotProfileDataEntity botProfile = discordBotProfileDataRepository.save(DiscordBotProfileDataEntity.builder()
            .ownerId(ownerChannel)
            .alias("Main bot")
            .username("Notifier")
            .avatarUrl("bot-profiles/owner/avatar.png")
            .build());

        subscriptionFormRepository.save(ChzzkSubscriptionFormEntity.builder()
            .chzzkChannelEntity(ownerChannel)
            .formOwner(ownerChannel)
            .chzzkSubscriptionType(ChzzkSubscriptionType.STREAM_OFFLINE)
            .webhookId(webhook)
            .botProfileId(botProfile)
            .languageIsoData(LanguageIsoData.Korean)
            .intervalMinute(10)
            .enabled(true)
            .colorHex("000000")
            .build());

        mockMvc.perform(delete("/api/v1/discord/webhooks/{id}", webhook.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER")))
            .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/v1/discord/bot-profiles/{id}", botProfile.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER")))
            .andExpect(status().isConflict());
    }

    @Test
    void userCannotDeleteResourcesReferencedOnlyBySoopSubscription() throws Exception {
        final DiscordWebhookDataEntity webhook = discordWebhookDataRepository.save(DiscordWebhookDataEntity.builder()
            .ownerId(ownerChannel)
            .name("Main webhook")
            .webhookUrl("https://example.test/webhook/soop")
            .build());
        final DiscordBotProfileDataEntity botProfile = discordBotProfileDataRepository.save(DiscordBotProfileDataEntity.builder()
            .ownerId(ownerChannel)
            .alias("Main bot")
            .username("Notifier")
            .avatarUrl("bot-profiles/owner/avatar.png")
            .build());
        soopSubscriptionRepository.save(SoopSubscriptionEntity.builder()
            .soopUserId("soop123")
            .soopChannelName("SOOP streamer")
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

        mockMvc.perform(delete("/api/v1/discord/webhooks/{id}", webhook.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER")))
            .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/v1/discord/bot-profiles/{id}", botProfile.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER")))
            .andExpect(status().isConflict());
    }

    private ChzzkChannelEntity createChannel(String channelId, String channelName) {
        return chzzkChannelRepository.saveAndFlush(ChzzkChannelEntity.builder()
            .channelId(channelId)
            .channelName(channelName)
            .profileUrl("https://example.test/profile/" + channelId)
            .isVerifiedMark(false)
            .channelDescription("desc-" + channelId)
            .subscriptionAvailability(true)
            .isLive(false)
            .followerCount(0)
            .lastCheckTime(ZonedDateTime.now(ZoneId.of("UTC")))
            .build());
    }
}
