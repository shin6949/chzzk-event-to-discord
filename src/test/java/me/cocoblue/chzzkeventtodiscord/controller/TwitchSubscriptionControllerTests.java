package me.cocoblue.chzzkeventtodiscord.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.cocoblue.chzzkeventtodiscord.domain.twitch.TwitchEventSubscriptionEntity;
import me.cocoblue.chzzkeventtodiscord.domain.twitch.TwitchEventSubscriptionRepository;
import me.cocoblue.chzzkeventtodiscord.dto.discord.DiscordEmbed;
import me.cocoblue.chzzkeventtodiscord.dto.twitch.TwitchEventSubDtos;
import me.cocoblue.chzzkeventtodiscord.service.DiscordWebhookService;
import me.cocoblue.chzzkeventtodiscord.service.twitch.TwitchApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TwitchSubscriptionControllerTests {
    private static final String OWNER_CHANNEL_ID = "owner-channel";
    private static final String OTHER_CHANNEL_ID = "other-channel";
    private static final String EVENTSUB_SECRET = "test-eventsub-secret";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TwitchEventSubscriptionRepository repository;

    @MockBean
    private TwitchApiClient twitchApiClient;
    @MockBean
    private DiscordWebhookService discordWebhookService;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void userCanCreateAndListOwnedTwitchSubscriptions() throws Exception {
        when(twitchApiClient.resolveBroadcaster(eq("streamer"), any()))
            .thenReturn(new TwitchEventSubDtos.TwitchUser("1234", "streamer", "Streamer"));
        when(twitchApiClient.createStreamSubscription("stream.online", "1234")).thenReturn("online-sub-id");
        when(twitchApiClient.createStreamSubscription("stream.offline", "1234")).thenReturn("offline-sub-id");

        final String payload = objectMapper.writeValueAsString(Map.of(
            "broadcasterLogin", "streamer",
            "discordWebhookUrl", "https://discord.com/api/webhooks/test",
            "enabled", true
        ));

        mockMvc.perform(post("/api/v1/twitch/subscriptions")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.ownerChannelId").value(OWNER_CHANNEL_ID))
            .andExpect(jsonPath("$.broadcasterUserId").value("1234"))
            .andExpect(jsonPath("$.eventsubOnlineId").value("online-sub-id"))
            .andExpect(jsonPath("$.eventsubOfflineId").value("offline-sub-id"));

        repository.save(TwitchEventSubscriptionEntity.builder()
            .ownerChannelId(OTHER_CHANNEL_ID)
            .broadcasterUserId("9999")
            .broadcasterLogin("other")
            .broadcasterDisplayName("Other")
            .discordWebhookUrl("https://discord.com/api/webhooks/other")
            .enabled(true)
            .build());

        mockMvc.perform(get("/api/v1/twitch/subscriptions")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].broadcasterLogin").value("streamer"));
    }

    @Test
    void userCannotDeleteOtherOwnersTwitchSubscription() throws Exception {
        final TwitchEventSubscriptionEntity entity = repository.save(TwitchEventSubscriptionEntity.builder()
            .ownerChannelId(OTHER_CHANNEL_ID)
            .broadcasterUserId("9999")
            .broadcasterLogin("other")
            .broadcasterDisplayName("Other")
            .discordWebhookUrl("https://discord.com/api/webhooks/other")
            .enabled(true)
            .build());

        mockMvc.perform(delete("/api/v1/twitch/subscriptions/{id}", entity.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER")))
            .andExpect(status().isForbidden());
    }

    @Test
    void eventSubVerificationReturnsChallengeForValidSignature() throws Exception {
        final String body = objectMapper.writeValueAsString(Map.of("challenge", "challenge-token"));

        mockMvc.perform(post("/api/v1/twitch/eventsub")
                .contentType(MediaType.APPLICATION_JSON)
                .with(twitchHeaders(body, "webhook_callback_verification"))
                .content(body))
            .andExpect(status().isOk())
            .andExpect(content().string("challenge-token"));
    }

    @Test
    void eventSubNotificationSendsDiscordWebhook() throws Exception {
        repository.save(TwitchEventSubscriptionEntity.builder()
            .ownerChannelId(OWNER_CHANNEL_ID)
            .broadcasterUserId("1234")
            .broadcasterLogin("streamer")
            .broadcasterDisplayName("Streamer")
            .discordWebhookUrl("https://discord.com/api/webhooks/test")
            .enabled(true)
            .build());
        final String body = objectMapper.writeValueAsString(Map.of(
            "subscription", Map.of("type", "stream.online"),
            "event", Map.of(
                "broadcaster_user_id", "1234",
                "broadcaster_user_login", "streamer",
                "broadcaster_user_name", "Streamer"
            )
        ));

        mockMvc.perform(post("/api/v1/twitch/eventsub")
                .contentType(MediaType.APPLICATION_JSON)
                .with(twitchHeaders(body, "notification"))
                .content(body))
            .andExpect(status().isNoContent());

        verify(discordWebhookService).sendDiscordWebhook(any(DiscordEmbed.Webhook.class), eq("https://discord.com/api/webhooks/test"));
    }

    private RequestPostProcessor twitchHeaders(String body, String messageType) {
        return request -> {
            final String messageId = "message-id";
            final String timestamp = "2026-06-12T00:00:00Z";
            request.addHeader("Twitch-Eventsub-Message-Id", messageId);
            request.addHeader("Twitch-Eventsub-Message-Timestamp", timestamp);
            request.addHeader("Twitch-Eventsub-Message-Type", messageType);
            request.addHeader("Twitch-Eventsub-Message-Signature", "sha256=" + hmac(messageId + timestamp + body));
            return request;
        };
    }

    private String hmac(String message) {
        try {
            final Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(EVENTSUB_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
