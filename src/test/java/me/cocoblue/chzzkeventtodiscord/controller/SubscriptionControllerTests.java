package me.cocoblue.chzzkeventtodiscord.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import me.cocoblue.chzzkeventtodiscord.domain.eventlog.NotificationLogEntity;
import me.cocoblue.chzzkeventtodiscord.domain.eventlog.NotificationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code SubscriptionControllerTests}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 7fec3f1.
 *
 * @since unreleased after Ver.0.1.4
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SubscriptionControllerTests {
  private static final String OWNER_CHANNEL_ID = "owner-channel-1";
  private static final String OTHER_OWNER_CHANNEL_ID = "owner-channel-2";
  private static final String TARGET_CHANNEL_ID = "target-channel";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private ChzzkChannelRepository chzzkChannelRepository;
  @Autowired private ChzzkSubscriptionFormRepository chzzkSubscriptionFormRepository;
  @Autowired private NotificationLogRepository notificationLogRepository;
  @Autowired private DiscordWebhookDataRepository discordWebhookDataRepository;
  @Autowired private DiscordBotProfileDataRepository discordBotProfileDataRepository;

  private DiscordWebhookDataEntity ownerWebhook;
  private DiscordBotProfileDataEntity ownerBotProfile;
  private DiscordWebhookDataEntity otherOwnerWebhook;
  private DiscordBotProfileDataEntity otherOwnerBotProfile;
  private ChzzkSubscriptionFormEntity ownerSubscription;
  private ChzzkSubscriptionFormEntity otherOwnerSubscription;

  /**
   * {@code setUp}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @BeforeEach
  void setUp() {
    notificationLogRepository.deleteAll();
    chzzkSubscriptionFormRepository.deleteAll();
    discordBotProfileDataRepository.deleteAll();
    discordWebhookDataRepository.deleteAll();
    chzzkChannelRepository.deleteAll();

    createChannel(OWNER_CHANNEL_ID, "Owner One");
    createChannel(OTHER_OWNER_CHANNEL_ID, "Owner Two");
    createChannel(TARGET_CHANNEL_ID, "Target");

    final ChzzkChannelEntity ownerChannel =
        chzzkChannelRepository.getReferenceById(OWNER_CHANNEL_ID);
    final ChzzkChannelEntity otherOwnerChannel =
        chzzkChannelRepository.getReferenceById(OTHER_OWNER_CHANNEL_ID);
    final ChzzkChannelEntity targetChannel =
        chzzkChannelRepository.getReferenceById(TARGET_CHANNEL_ID);

    ownerWebhook =
        discordWebhookDataRepository.save(
            DiscordWebhookDataEntity.builder()
                .name("owner webhook")
                .webhookUrl(discordWebhookUrl("100000000000000101", "owner-token"))
                .ownerId(ownerChannel)
                .build());
    ownerBotProfile =
        discordBotProfileDataRepository.save(
            DiscordBotProfileDataEntity.builder()
                .ownerId(ownerChannel)
                .username("owner bot")
                .avatarUrl("https://example.test/avatar/owner")
                .build());

    otherOwnerWebhook =
        discordWebhookDataRepository.save(
            DiscordWebhookDataEntity.builder()
                .name("other webhook")
                .webhookUrl(discordWebhookUrl("100000000000000102", "other-token"))
                .ownerId(otherOwnerChannel)
                .build());
    otherOwnerBotProfile =
        discordBotProfileDataRepository.save(
            DiscordBotProfileDataEntity.builder()
                .ownerId(otherOwnerChannel)
                .username("other bot")
                .avatarUrl("https://example.test/avatar/other")
                .build());

    ownerSubscription =
        chzzkSubscriptionFormRepository.save(
            ChzzkSubscriptionFormEntity.builder()
                .chzzkChannelEntity(ownerChannel)
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

    otherOwnerSubscription =
        chzzkSubscriptionFormRepository.save(
            ChzzkSubscriptionFormEntity.builder()
                .chzzkChannelEntity(otherOwnerChannel)
                .formOwner(otherOwnerChannel)
                .chzzkSubscriptionType(ChzzkSubscriptionType.CHANNEL_UPDATE)
                .webhookId(otherOwnerWebhook)
                .botProfileId(otherOwnerBotProfile)
                .languageIsoData(LanguageIsoData.English)
                .intervalMinute(20)
                .enabled(true)
                .colorHex("123456")
                .content("other content")
                .build());
  }

  /**
   * {@code listForUserReturnsOnlyOwnedSubscriptions}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void listForUserReturnsOnlyOwnedSubscriptions() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/subscriptions")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.number").value(0))
        .andExpect(jsonPath("$.size").value(20))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.first").value(true))
        .andExpect(jsonPath("$.last").value(true))
        .andExpect(jsonPath("$.pageable").doesNotExist())
        .andExpect(jsonPath("$.sort").doesNotExist())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].id").value(ownerSubscription.getId()))
        .andExpect(jsonPath("$.content[0].formOwnerChannelId").value(OWNER_CHANNEL_ID))
        .andExpect(jsonPath("$.content[0].createdAt", not(nullValue())))
        .andExpect(jsonPath("$.content[0].lastNotificationSentAt").doesNotExist());
  }

  /**
   * {@code listIncludesLatestNotificationTimestamp}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void listIncludesLatestNotificationTimestamp() throws Exception {
    notificationLogRepository.saveAndFlush(
        NotificationLogEntity.builder().subscriptionForm(ownerSubscription).build());
    final NotificationLogEntity latestLog =
        notificationLogRepository.saveAndFlush(
            NotificationLogEntity.builder().subscriptionForm(ownerSubscription).build());

    final String responseBody =
        mockMvc
            .perform(
                get("/api/v1/subscriptions")
                    .with(
                        SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].createdAt", not(nullValue())))
            .andExpect(jsonPath("$.content[0].lastNotificationSentAt", not(nullValue())))
            .andReturn()
            .getResponse()
            .getContentAsString();

    final ZonedDateTime responseLastNotificationSentAt =
        ZonedDateTime.parse(
            objectMapper
                .readTree(responseBody)
                .get("content")
                .get(0)
                .get("lastNotificationSentAt")
                .asText());
    assertEquals(latestLog.getCreatedAt().toInstant(), responseLastNotificationSentAt.toInstant());
  }

  /**
   * {@code createReturnsBadRequestWhenRequiredFieldsAreMissing}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-02-17 06:57:49 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d252252.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void createReturnsBadRequestWhenRequiredFieldsAreMissing() throws Exception {
    final String missingTypePayload =
        objectMapper.writeValueAsString(
            Map.of(
                "webhookId", ownerWebhook.getId(),
                "botProfileId", ownerBotProfile.getId()));
    mockMvc
        .perform(
            post("/api/v1/subscriptions")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(missingTypePayload))
        .andExpect(status().isBadRequest());

    final String adminMissingOwnerPayload =
        objectMapper.writeValueAsString(
            Map.of(
                "channelId",
                TARGET_CHANNEL_ID,
                "subscriptionType",
                "STREAM_OFFLINE",
                "webhookId",
                ownerWebhook.getId(),
                "botProfileId",
                ownerBotProfile.getId()));
    mockMvc
        .perform(
            post("/api/v1/subscriptions")
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(adminMissingOwnerPayload))
        .andExpect(status().isBadRequest());
  }

  /**
   * {@code userCreateDefaultsTargetChannelToAuthenticatedChannel}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void userCreateDefaultsTargetChannelToAuthenticatedChannel() throws Exception {
    final String createPayload =
        objectMapper.writeValueAsString(
            Map.of(
                "subscriptionType", "STREAM_OFFLINE",
                "webhookId", ownerWebhook.getId(),
                "botProfileId", ownerBotProfile.getId()));

    mockMvc
        .perform(
            post("/api/v1/subscriptions")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.channelId").value(OWNER_CHANNEL_ID))
        .andExpect(jsonPath("$.formOwnerChannelId").value(OWNER_CHANNEL_ID));
  }

  /**
   * {@code userCannotCreateSubscriptionForAnotherTargetChannel}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void userCannotCreateSubscriptionForAnotherTargetChannel() throws Exception {
    final String createPayload =
        objectMapper.writeValueAsString(
            Map.of(
                "channelId",
                TARGET_CHANNEL_ID,
                "subscriptionType",
                "STREAM_OFFLINE",
                "webhookId",
                ownerWebhook.getId(),
                "botProfileId",
                ownerBotProfile.getId()));

    mockMvc
        .perform(
            post("/api/v1/subscriptions")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload))
        .andExpect(status().isForbidden());
  }

  /**
   * {@code userCannotCreateSubscriptionWithOtherOwnersDiscordResources}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void userCannotCreateSubscriptionWithOtherOwnersDiscordResources() throws Exception {
    final String otherWebhookPayload =
        objectMapper.writeValueAsString(
            Map.of(
                "subscriptionType", "STREAM_OFFLINE",
                "webhookId", otherOwnerWebhook.getId(),
                "botProfileId", ownerBotProfile.getId()));

    mockMvc
        .perform(
            post("/api/v1/subscriptions")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(otherWebhookPayload))
        .andExpect(status().isForbidden());

    final String otherBotProfilePayload =
        objectMapper.writeValueAsString(
            Map.of(
                "subscriptionType", "STREAM_OFFLINE",
                "webhookId", ownerWebhook.getId(),
                "botProfileId", otherOwnerBotProfile.getId()));

    mockMvc
        .perform(
            post("/api/v1/subscriptions")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(otherBotProfilePayload))
        .andExpect(status().isForbidden());
  }

  /**
   * {@code createRejectsInvalidClientControlledValidationFields}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void createRejectsInvalidClientControlledValidationFields() throws Exception {
    final String tooSmallIntervalPayload =
        objectMapper.writeValueAsString(
            Map.of(
                "subscriptionType",
                "STREAM_OFFLINE",
                "webhookId",
                ownerWebhook.getId(),
                "botProfileId",
                ownerBotProfile.getId(),
                "intervalMinute",
                0));
    mockMvc
        .perform(
            post("/api/v1/subscriptions")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(tooSmallIntervalPayload))
        .andExpect(status().isBadRequest());

    final String tooLargeIntervalPayload =
        objectMapper.writeValueAsString(
            Map.of(
                "subscriptionType",
                "STREAM_OFFLINE",
                "webhookId",
                ownerWebhook.getId(),
                "botProfileId",
                ownerBotProfile.getId(),
                "intervalMinute",
                1441));
    mockMvc
        .perform(
            post("/api/v1/subscriptions")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(tooLargeIntervalPayload))
        .andExpect(status().isBadRequest());

    final String invalidColorPayload =
        objectMapper.writeValueAsString(
            Map.of(
                "subscriptionType",
                "STREAM_OFFLINE",
                "webhookId",
                ownerWebhook.getId(),
                "botProfileId",
                ownerBotProfile.getId(),
                "colorHex",
                "javascript:alert(1)"));
    mockMvc
        .perform(
            post("/api/v1/subscriptions")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidColorPayload))
        .andExpect(status().isBadRequest());
  }

  /**
   * {@code createAcceptsMaximumSafeClientControlledValidationFields}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void createAcceptsMaximumSafeClientControlledValidationFields() throws Exception {
    final String createPayload =
        objectMapper.writeValueAsString(
            Map.of(
                "subscriptionType",
                "STREAM_OFFLINE",
                "webhookId",
                ownerWebhook.getId(),
                "botProfileId",
                ownerBotProfile.getId(),
                "intervalMinute",
                1440,
                "content",
                "x".repeat(2000),
                "colorHex",
                "ABCDEF"));

    mockMvc
        .perform(
            post("/api/v1/subscriptions")
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.intervalMinute").value(1440))
        .andExpect(jsonPath("$.colorHex").value("ABCDEF"));
  }

  /**
   * {@code userCannotReadOrModifyOtherOwnersSubscriptions}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void userCannotReadOrModifyOtherOwnersSubscriptions() throws Exception {
    final String updatePayload = objectMapper.writeValueAsString(Map.of("content", "new content"));

    mockMvc
        .perform(
            get("/api/v1/subscriptions/{id}", otherOwnerSubscription.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER")))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(
            put("/api/v1/subscriptions/{id}", otherOwnerSubscription.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(
            delete("/api/v1/subscriptions/{id}", otherOwnerSubscription.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isForbidden());
  }

  /**
   * {@code adminCanListAndReadAnySubscription}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void adminCanListAndReadAnySubscription() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/subscriptions")
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.number").value(0))
        .andExpect(jsonPath("$.size").value(20))
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.content", hasSize(2)));

    mockMvc
        .perform(
            get("/api/v1/subscriptions/{id}", otherOwnerSubscription.getId())
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(otherOwnerSubscription.getId()))
        .andExpect(jsonPath("$.formOwnerChannelId").value(OTHER_OWNER_CHANNEL_ID));
  }

  /**
   * {@code userCanCreateUpdateAndDeleteOwnedSubscription}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void userCanCreateUpdateAndDeleteOwnedSubscription() throws Exception {
    final String createPayload =
        objectMapper.writeValueAsString(
            Map.of(
                "channelId",
                OWNER_CHANNEL_ID,
                "subscriptionType",
                "STREAM_OFFLINE",
                "webhookId",
                ownerWebhook.getId(),
                "botProfileId",
                ownerBotProfile.getId(),
                "language",
                "Korean",
                "intervalMinute",
                15,
                "enabled",
                true,
                "content",
                "created content",
                "colorHex",
                "ABCDEF"));

    final String createResponseBody =
        mockMvc
            .perform(
                post("/api/v1/subscriptions")
                    .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", not(nullValue())))
            .andExpect(jsonPath("$.channelId").value(OWNER_CHANNEL_ID))
            .andExpect(jsonPath("$.formOwnerChannelId").value(OWNER_CHANNEL_ID))
            .andReturn()
            .getResponse()
            .getContentAsString();

    final Long createdId = objectMapper.readTree(createResponseBody).get("id").asLong();

    final String updatePayload =
        objectMapper.writeValueAsString(Map.of("content", "updated content", "enabled", false));

    mockMvc
        .perform(
            put("/api/v1/subscriptions/{id}", createdId)
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("updated content"))
        .andExpect(jsonPath("$.enabled").value(false));

    mockMvc
        .perform(
            delete("/api/v1/subscriptions/{id}", createdId)
                .with(SecurityMockMvcRequestPostProcessors.user(OWNER_CHANNEL_ID).roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isNoContent());

    assertFalse(chzzkSubscriptionFormRepository.findById(createdId).isPresent());
  }

  /**
   * {@code unauthenticatedRequestsAreRejected}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void unauthenticatedRequestsAreRejected() throws Exception {
    mockMvc.perform(get("/api/v1/subscriptions")).andExpect(status().isUnauthorized());
  }

  /**
   * {@code createChannel}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
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
    return "https://" + "discord.com" + "/api/" + "webhooks/" + id + "/" + token;
  }
}
