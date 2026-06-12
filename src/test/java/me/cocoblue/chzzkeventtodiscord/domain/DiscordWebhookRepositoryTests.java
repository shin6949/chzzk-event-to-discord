package me.cocoblue.chzzkeventtodiscord.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.Optional;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

/**
 * {@code DiscordWebhookRepositoryTests}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2024-03-04 02:28:28 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 db09ddc.
 *
 * @since Ver.0.1
 */
@DataJpaTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DiscordWebhookRepositoryTests {
  @Autowired private DiscordWebhookDataRepository discordWebhookDataRepository;

  @Autowired private ChzzkChannelRepository chzzkChannelRepository;

  @Autowired private TestEntityManager testEntityManager;

  private final String TEST_CHANNEL_ID = "testChannelId";

  /**
   * {@code setUp}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2024-03-04 02:28:28 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 db09ddc.
   *
   * @since Ver.0.1
   */
  @BeforeAll
  void setUp() {
    final ChzzkChannelEntity chzzkChannelEntity =
        ChzzkChannelEntity.builder()
            .channelId(TEST_CHANNEL_ID)
            .channelName("testChannelName")
            .profileUrl("testProfileUrl")
            .isVerifiedMark(true)
            .channelDescription("testChannelDescription")
            .isLive(true)
            .followerCount(100)
            .lastCheckTime(ZonedDateTime.now())
            .build();
    chzzkChannelRepository.save(chzzkChannelEntity);
  }

  /**
   * {@code saveTest}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2024-03-04 02:28:28 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 db09ddc.
   *
   * @since Ver.0.1
   */
  @Test
  @Transactional
  @DisplayName("DiscordWebhookDataEntity 저장 테스트")
  void saveTest() {
    final Optional<ChzzkChannelEntity> chzzkChannelEntity =
        chzzkChannelRepository.findById(TEST_CHANNEL_ID);
    assertTrue(chzzkChannelEntity.isPresent());

    final String webhookName = "testName";
    final String webhookUrl = "testWebhookUrl";
    final String meno = "testMeno";

    final DiscordWebhookDataEntity entity =
        DiscordWebhookDataEntity.builder()
            .name(webhookName)
            .webhookUrl(webhookUrl)
            .meno(meno)
            .ownerId(chzzkChannelEntity.get())
            .build();
    final DiscordWebhookDataEntity savedEntity = discordWebhookDataRepository.save(entity);
    final DiscordWebhookDataEntity foundEntity =
        testEntityManager.find(DiscordWebhookDataEntity.class, savedEntity.getId());

    assertEquals(webhookName, foundEntity.getName());
    assertEquals(webhookUrl, foundEntity.getWebhookUrl());
    assertEquals(meno, foundEntity.getMeno());
  }

  /**
   * {@code findDiscordWebhookDataEntityByWebhookUrlAndNameAndOwnerId_Test}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-03-09 22:32:50 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.2, 근거 커밋 04ac179.
   *
   * @since Ver.0.1.2
   */
  @Test
  @Transactional
  @DisplayName("findDiscordWebhookDataEntityByWebhookUrlAndNameAndOwnerId 메소드 테스트")
  void findDiscordWebhookDataEntityByWebhookUrlAndNameAndOwnerId_Test() {
    final Optional<ChzzkChannelEntity> chzzkChannelEntity =
        chzzkChannelRepository.findById(TEST_CHANNEL_ID);
    assertTrue(chzzkChannelEntity.isPresent());

    final String webhookName = "testName";
    final String webhookUrl = "testWebhookUrl";
    final String meno = "testMeno";

    final DiscordWebhookDataEntity entity =
        DiscordWebhookDataEntity.builder()
            .name(webhookName)
            .webhookUrl(webhookUrl)
            .meno(meno)
            .ownerId(chzzkChannelEntity.get())
            .build();
    discordWebhookDataRepository.save(entity);

    final Optional<DiscordWebhookDataEntity> foundEntity =
        discordWebhookDataRepository.findDiscordWebhookDataEntityByWebhookUrlAndNameAndOwnerId(
            webhookUrl, webhookName, chzzkChannelEntity.get());

    assertTrue(foundEntity.isPresent());
    assertEquals(webhookName, foundEntity.get().getName());
  }
}
