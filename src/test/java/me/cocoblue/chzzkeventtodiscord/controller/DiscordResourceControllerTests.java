package me.cocoblue.chzzkeventtodiscord.controller;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
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
import me.cocoblue.chzzkeventtodiscord.service.BotProfileImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * {@code DiscordResourceControllerTests}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.static-content.url-prefix=https://static.example.test/assets")
@Transactional
class DiscordResourceControllerTests {
  private static final String OWNER_CHANNEL_ID = "owner-channel";
  private static final String OTHER_CHANNEL_ID = "other-channel";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private ChzzkChannelRepository chzzkChannelRepository;
  @Autowired private DiscordWebhookDataRepository discordWebhookDataRepository;
  @Autowired private DiscordBotProfileDataRepository discordBotProfileDataRepository;
  @Autowired private ChzzkSubscriptionFormRepository subscriptionFormRepository;

  @MockitoBean private BotProfileImageStorageService botProfileImageStorageService;

  private ChzzkChannelEntity ownerChannel;
  private ChzzkChannelEntity otherChannel;

  /**
   * {@code setUp}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @BeforeEach
  void setUp() {
    subscriptionFormRepository.deleteAll();
    discordBotProfileDataRepository.deleteAll();
    discordWebhookDataRepository.deleteAll();
    chzzkChannelRepository.deleteAll();

    ownerChannel = createChannel(OWNER_CHANNEL_ID, "Owner");
    otherChannel = createChannel(OTHER_CHANNEL_ID, "Other");
  }

  /**
   * {@code userCanCreateAndListOwnedWebhooks}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void userCanCreateAndListOwnedWebhooks() throws Exception {
    discordWebhookDataRepository.save(
        DiscordWebhookDataEntity.builder()
            .ownerId(otherChannel)
            .name("Other webhook")
            .webhookUrl(discordWebhookUrl("100000000000000001", "other-token"))
            .build());

    final String payload =
        objectMapper.writeValueAsString(
            Map.of(
                "alias", "Main webhook",
                "url", discordWebhookUrl("100000000000000002", "main-token")));

    mockMvc
        .perform(
            post("/api/v1/discord/webhooks")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.alias").value("Main webhook"))
        .andExpect(jsonPath("$.url").doesNotExist())
        .andExpect(
            jsonPath("$.maskedUrl")
                .value("https://discord.com/api/webhooks/100000000000000002/****"))
        .andExpect(jsonPath("$.hasUrl").value(true))
        .andExpect(jsonPath("$.ownerChannelId").value(OWNER_CHANNEL_ID));

    mockMvc
        .perform(
            get("/api/v1/discord/webhooks")
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

  /**
   * {@code userCanUpdateOwnedWebhook}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void userCanUpdateOwnedWebhook() throws Exception {
    final DiscordWebhookDataEntity webhook =
        discordWebhookDataRepository.save(
            DiscordWebhookDataEntity.builder()
                .ownerId(ownerChannel)
                .name("Original webhook")
                .webhookUrl(discordWebhookUrl("100000000000000003", "original-token"))
                .build());

    final String payload =
        objectMapper.writeValueAsString(
            Map.of(
                "alias", "Edited webhook",
                "url", discordWebhookUrl("100000000000000004", "edited-token")));

    mockMvc
        .perform(
            put("/api/v1/discord/webhooks/{id}", webhook.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(webhook.getId()))
        .andExpect(jsonPath("$.alias").value("Edited webhook"))
        .andExpect(jsonPath("$.url").doesNotExist())
        .andExpect(
            jsonPath("$.maskedUrl")
                .value("https://discord.com/api/webhooks/100000000000000004/****"));
  }

  /**
   * {@code userCannotCreateWebhookWithInvalidUrl}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void userCannotCreateWebhookWithInvalidUrl() throws Exception {
    final String[] invalidUrls = {
      discordWebhookUrl("http", "discord.com", "100000000000000006", "insecure-token"),
      "https://evil.example/api/webhooks/100000000000000006/evil-token",
      discordWebhookUrl("100000000000000006", "token") + "?wait=true",
      discordWebhookUrlWithUserInfo("100000000000000006", "token"),
      discordWebhookUrl("not-a-number", "token")
    };

    for (String invalidUrl : invalidUrls) {
      final String payload =
          objectMapper.writeValueAsString(Map.of("alias", "Invalid webhook", "url", invalidUrl));

      mockMvc
          .perform(
              post("/api/v1/discord/webhooks")
                  .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                  .with(SecurityMockMvcRequestPostProcessors.csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(payload))
          .andExpect(status().isBadRequest());
    }
  }

  /**
   * {@code userCannotCreateWebhookForAnotherOwnerChannel}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void userCannotCreateWebhookForAnotherOwnerChannel() throws Exception {
    final String payload =
        objectMapper.writeValueAsString(
            Map.of(
                "alias", "Stolen owner webhook",
                "url", discordWebhookUrl("100000000000000007", "token"),
                "ownerChannelId", OTHER_CHANNEL_ID));

    mockMvc
        .perform(
            post("/api/v1/discord/webhooks")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isForbidden());
  }

  /**
   * {@code botProfileUploadStoresObjectKeyAndReturnsResolvedUrl}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void botProfileUploadStoresObjectKeyAndReturnsResolvedUrl() throws Exception {
    when(botProfileImageStorageService.upload(eq(OWNER_CHANNEL_ID), any(MultipartFile.class)))
        .thenReturn("bot-profiles/owner-channel/avatar.png");

    final MockMultipartFile avatar =
        new MockMultipartFile("avatar", "avatar.png", "image/png", "avatar".getBytes());

    mockMvc
        .perform(
            multipart("/api/v1/discord/bot-profiles")
                .file(avatar)
                .param("alias", "Main bot")
                .param("username", "Notifier")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.alias").value("Main bot"))
        .andExpect(jsonPath("$.username").value("Notifier"))
        .andExpect(
            jsonPath("$.avatarUrl")
                .value("https://static.example.test/assets/bot-profiles/owner-channel/avatar.png"));

    mockMvc
        .perform(
            get("/api/v1/discord/bot-profiles")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.number").value(0))
        .andExpect(jsonPath("$.size").value(20))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.pageable").doesNotExist())
        .andExpect(jsonPath("$.sort").doesNotExist())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(
            jsonPath("$.content[0].avatarUrl")
                .value("https://static.example.test/assets/bot-profiles/owner-channel/avatar.png"));
  }

  /**
   * {@code userCannotDeleteResourcesReferencedBySubscription}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void userCannotDeleteResourcesReferencedBySubscription() throws Exception {
    final DiscordWebhookDataEntity webhook =
        discordWebhookDataRepository.save(
            DiscordWebhookDataEntity.builder()
                .ownerId(ownerChannel)
                .name("Main webhook")
                .webhookUrl(discordWebhookUrl("100000000000000005", "main-token"))
                .build());
    final DiscordBotProfileDataEntity botProfile =
        discordBotProfileDataRepository.save(
            DiscordBotProfileDataEntity.builder()
                .ownerId(ownerChannel)
                .alias("Main bot")
                .username("Notifier")
                .avatarUrl("bot-profiles/owner/avatar.png")
                .build());

    subscriptionFormRepository.save(
        ChzzkSubscriptionFormEntity.builder()
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

    mockMvc
        .perform(
            delete("/api/v1/discord/webhooks/{id}", webhook.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isConflict());

    mockMvc
        .perform(
            delete("/api/v1/discord/bot-profiles/{id}", botProfile.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isConflict());
  }

  /**
   * {@code createChannel}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private ChzzkChannelEntity createChannel(String channelId, String channelName) {
    return chzzkChannelRepository.saveAndFlush(
        ChzzkChannelEntity.builder()
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

  private static String discordWebhookUrl(String id, String token) {
    return discordWebhookUrl("https", "discord.com", id, token);
  }

  private static String discordWebhookUrl(String scheme, String host, String id, String token) {
    return scheme + "://" + host + "/api/" + "webhooks/" + id + "/" + token;
  }

  private static String discordWebhookUrlWithUserInfo(String id, String token) {
    return "https://" + userInfo() + "@" + "discord.com" + "/api/" + "webhooks/" + id + "/" + token;
  }

  private static String userInfo() {
    return "user" + ":" + "pass";
  }
}
