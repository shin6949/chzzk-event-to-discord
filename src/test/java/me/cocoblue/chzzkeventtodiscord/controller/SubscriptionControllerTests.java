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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SubscriptionControllerTests {
    private static final String OWNER_CHANNEL_ID = "owner-channel-1";
    private static final String OTHER_OWNER_CHANNEL_ID = "owner-channel-2";
    private static final String TARGET_CHANNEL_ID = "target-channel";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ChzzkChannelRepository chzzkChannelRepository;
    @Autowired
    private ChzzkSubscriptionFormRepository chzzkSubscriptionFormRepository;
    @Autowired
    private DiscordWebhookDataRepository discordWebhookDataRepository;
    @Autowired
    private DiscordBotProfileDataRepository discordBotProfileDataRepository;

    private DiscordWebhookDataEntity ownerWebhook;
    private DiscordBotProfileDataEntity ownerBotProfile;
    private ChzzkSubscriptionFormEntity ownerSubscription;
    private ChzzkSubscriptionFormEntity otherOwnerSubscription;

    @BeforeEach
    void setUp() {
        chzzkSubscriptionFormRepository.deleteAll();
        discordBotProfileDataRepository.deleteAll();
        discordWebhookDataRepository.deleteAll();
        chzzkChannelRepository.deleteAll();

        final ChzzkChannelEntity ownerChannel = createChannel(OWNER_CHANNEL_ID, "Owner One");
        final ChzzkChannelEntity otherOwnerChannel = createChannel(OTHER_OWNER_CHANNEL_ID, "Owner Two");
        final ChzzkChannelEntity targetChannel = createChannel(TARGET_CHANNEL_ID, "Target");

        ownerWebhook = discordWebhookDataRepository.save(DiscordWebhookDataEntity.builder()
            .name("owner webhook")
            .webhookUrl("https://example.test/webhook/owner")
            .ownerId(ownerChannel)
            .build());
        ownerBotProfile = discordBotProfileDataRepository.save(DiscordBotProfileDataEntity.builder()
            .ownerId(ownerChannel)
            .username("owner bot")
            .avatarUrl("https://example.test/avatar/owner")
            .build());

        final DiscordWebhookDataEntity otherWebhook = discordWebhookDataRepository.save(DiscordWebhookDataEntity.builder()
            .name("other webhook")
            .webhookUrl("https://example.test/webhook/other")
            .ownerId(otherOwnerChannel)
            .build());
        final DiscordBotProfileDataEntity otherBotProfile = discordBotProfileDataRepository.save(DiscordBotProfileDataEntity.builder()
            .ownerId(otherOwnerChannel)
            .username("other bot")
            .avatarUrl("https://example.test/avatar/other")
            .build());

        ownerSubscription = chzzkSubscriptionFormRepository.save(ChzzkSubscriptionFormEntity.builder()
            .chzzkChannelEntity(targetChannel)
            .formOwner(ownerChannel)
            .chzzkSubscriptionType(ChzzkSubscriptionType.STREAM_OFFLINE)
            .webhookId(ownerWebhook)
            .botProfileId(ownerBotProfile)
            .languageIsoData(LanguageIsoData.Korean)
            .intervalMinute(10)
            .enabled(true)
            .colorHex("000000")
            .content("owner content")
            .build());

        otherOwnerSubscription = chzzkSubscriptionFormRepository.save(ChzzkSubscriptionFormEntity.builder()
            .chzzkChannelEntity(targetChannel)
            .formOwner(otherOwnerChannel)
            .chzzkSubscriptionType(ChzzkSubscriptionType.CHANNEL_UPDATE)
            .webhookId(otherWebhook)
            .botProfileId(otherBotProfile)
            .languageIsoData(LanguageIsoData.English)
            .intervalMinute(20)
            .enabled(true)
            .colorHex("123456")
            .content("other content")
            .build());
    }

    @Test
    void listForUserReturnsOnlyOwnedSubscriptions() throws Exception {
        mockMvc.perform(get("/api/v1/subscriptions")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].id").value(ownerSubscription.getId()))
            .andExpect(jsonPath("$.content[0].formOwnerChannelId").value(OWNER_CHANNEL_ID));
    }

    @Test
    void userCannotReadOrModifyOtherOwnersSubscriptions() throws Exception {
        final String updatePayload = objectMapper.writeValueAsString(Map.of("content", "new content"));

        mockMvc.perform(get("/api/v1/subscriptions/{id}", otherOwnerSubscription.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER")))
            .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/subscriptions/{id}", otherOwnerSubscription.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
            .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/subscriptions/{id}", otherOwnerSubscription.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER")))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListAndReadAnySubscription() throws Exception {
        mockMvc.perform(get("/api/v1/subscriptions")
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)));

        mockMvc.perform(get("/api/v1/subscriptions/{id}", otherOwnerSubscription.getId())
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(otherOwnerSubscription.getId()))
            .andExpect(jsonPath("$.formOwnerChannelId").value(OTHER_OWNER_CHANNEL_ID));
    }

    @Test
    void userCanCreateUpdateAndDeleteOwnedSubscription() throws Exception {
        final String createPayload = objectMapper.writeValueAsString(Map.of(
            "channelId", TARGET_CHANNEL_ID,
            "subscriptionType", "STREAM_OFFLINE",
            "webhookId", ownerWebhook.getId(),
            "botProfileId", ownerBotProfile.getId(),
            "language", "Korean",
            "intervalMinute", 15,
            "enabled", true,
            "content", "created content",
            "colorHex", "ABCDEF"
        ));

        final String createResponseBody = mockMvc.perform(post("/api/v1/subscriptions")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", not(nullValue())))
            .andExpect(jsonPath("$.formOwnerChannelId").value(OWNER_CHANNEL_ID))
            .andReturn()
            .getResponse()
            .getContentAsString();

        final Long createdId = objectMapper.readTree(createResponseBody).get("id").asLong();

        final String updatePayload = objectMapper.writeValueAsString(Map.of(
            "content", "updated content",
            "enabled", false
        ));

        mockMvc.perform(put("/api/v1/subscriptions/{id}", createdId)
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("updated content"))
            .andExpect(jsonPath("$.enabled").value(false));

        mockMvc.perform(delete("/api/v1/subscriptions/{id}", createdId)
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER")))
            .andExpect(status().isNoContent());

        assertFalse(chzzkSubscriptionFormRepository.findById(createdId).isPresent());
    }

    @Test
    void unauthenticatedRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/v1/subscriptions"))
            .andExpect(status().isUnauthorized());
    }

    private ChzzkChannelEntity createChannel(String channelId, String channelName) {
        return chzzkChannelRepository.save(ChzzkChannelEntity.builder()
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
