package me.cocoblue.chzzkeventtodiscord.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import me.cocoblue.chzzkeventtodiscord.data.LanguageIsoData;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkSubscriptionType;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * {@code ChzzkSubscriptionFormRepositoryTests}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2024-03-11 21:36:19 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.2, 근거 커밋 e0f6305.
 *
 * @since Ver.0.1.2
 */
@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ChzzkSubscriptionFormRepositoryTests {
  @Autowired private ChzzkSubscriptionFormRepository chzzkSubscriptionFormRepository;

  @Autowired private DiscordWebhookDataRepository discordWebhookDataRepository;

  @Autowired private DiscordBotProfileDataRepository discordBotProfileDataRepository;

  private final ChzzkChannelEntity COMMON_CHANNEL_ENTITY =
      ChzzkChannelEntity.builder()
          .channelId("123456789")
          .channelName("testChannelName")
          .lastCheckTime(ZonedDateTime.now())
          .build();

  private final DiscordWebhookDataEntity COMMON_WEBHOOK_ENTITY =
      DiscordWebhookDataEntity.builder()
          .name("testWebhook")
          .meno("This is test memo")
          .webhookUrl(discordWebhookUrl("1234567890", "abcdefghijk"))
          .ownerId(COMMON_CHANNEL_ENTITY)
          .build();

  private final DiscordBotProfileDataEntity COMMON_BOT_PROFILE_ENTITY =
      DiscordBotProfileDataEntity.builder()
          .avatarUrl("https://cocoblue.me/image/test.png")
          .username("testBot")
          .ownerId(COMMON_CHANNEL_ENTITY)
          .build();

  /**
   * {@code setUp}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2024-03-11 21:36:19 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.2, 근거 커밋 e0f6305.
   *
   * @since Ver.0.1.2
   */
  @BeforeAll
  void setUp() {
    discordWebhookDataRepository.save(COMMON_WEBHOOK_ENTITY);
    // save 시, id가 객체에 들어가야함.
    assertNotNull(COMMON_WEBHOOK_ENTITY.getId());

    discordBotProfileDataRepository.save(COMMON_BOT_PROFILE_ENTITY);
    assertNotNull(COMMON_BOT_PROFILE_ENTITY.getId());
    assertEquals(
        COMMON_BOT_PROFILE_ENTITY.getOwnerId().getChannelId(),
        COMMON_CHANNEL_ENTITY.getChannelId());
  }

  /**
   * {@code findAllByChzzkChannelEntityAndChzzkSubscriptionTypeAndEnabled_Test}은 필요한 데이터를 조회하거나
   * 해석합니다.
   *
   * <p>Git 이력: 생성 2024-03-11 21:36:19 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.2, 근거 커밋 e0f6305.
   *
   * @since Ver.0.1.2
   */
  @Test
  @Transactional
  @DisplayName("findAllByChzzkChannelEntityAndChzzkSubscriptionTypeAndEnabled 메소드 테스트")
  void findAllByChzzkChannelEntityAndChzzkSubscriptionTypeAndEnabled_Test() {
    final List<ChzzkSubscriptionFormEntity> chzzkSubscriptionFormEntityList = new ArrayList<>();
    chzzkSubscriptionFormEntityList.add(
        ChzzkSubscriptionFormEntity.builder()
            .chzzkChannelEntity(COMMON_CHANNEL_ENTITY)
            .chzzkSubscriptionType(ChzzkSubscriptionType.STREAM_ONLINE)
            .webhookId(COMMON_WEBHOOK_ENTITY)
            .formOwner(COMMON_CHANNEL_ENTITY)
            .languageIsoData(LanguageIsoData.Korean)
            .intervalMinute(10)
            .colorHex("FFFFFF")
            .enabled(true)
            .content("testContent")
            .botProfileId(COMMON_BOT_PROFILE_ENTITY)
            .build());

    chzzkSubscriptionFormEntityList.add(
        ChzzkSubscriptionFormEntity.builder()
            .chzzkChannelEntity(COMMON_CHANNEL_ENTITY)
            .chzzkSubscriptionType(ChzzkSubscriptionType.STREAM_ONLINE)
            .webhookId(COMMON_WEBHOOK_ENTITY)
            .formOwner(COMMON_CHANNEL_ENTITY)
            .languageIsoData(LanguageIsoData.Korean)
            .intervalMinute(10)
            .colorHex("FFFFFF")
            .enabled(false)
            .botProfileId(COMMON_BOT_PROFILE_ENTITY)
            .build());

    chzzkSubscriptionFormRepository.saveAll(chzzkSubscriptionFormEntityList);

    final List<ChzzkSubscriptionFormEntity> result =
        chzzkSubscriptionFormRepository
            .findAllByChzzkChannelEntityAndChzzkSubscriptionTypeAndEnabled(
                COMMON_CHANNEL_ENTITY, ChzzkSubscriptionType.STREAM_ONLINE, true);
    assertEquals(1, result.size());
    assertEquals(chzzkSubscriptionFormEntityList.get(0), result.get(0));

    final List<ChzzkSubscriptionFormEntity> result2 =
        chzzkSubscriptionFormRepository
            .findAllByChzzkChannelEntityAndChzzkSubscriptionTypeAndEnabled(
                COMMON_CHANNEL_ENTITY, ChzzkSubscriptionType.STREAM_ONLINE, false);
    assertEquals(1, result2.size());
    assertEquals(chzzkSubscriptionFormEntityList.get(1), result2.get(0));
  }

  private static String discordWebhookUrl(String id, String token) {
    return "https://" + "discord.com" + "/api/" + "webhooks/" + id + "/" + token;
  }
}
