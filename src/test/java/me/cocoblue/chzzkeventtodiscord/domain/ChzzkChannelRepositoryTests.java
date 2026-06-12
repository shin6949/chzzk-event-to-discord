package me.cocoblue.chzzkeventtodiscord.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * {@code ChzzkChannelRepositoryTests}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2024-03-11 17:02:16 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.2, 근거 커밋 6f9e7fb.
 *
 * @since Ver.0.1.2
 */
@Log4j2
@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ChzzkChannelRepositoryTests {
  @Autowired private ChzzkChannelRepository chzzkChannelRepository;

  private final String CHANNEL_ID = "testChannelId";
  final String CHANNEL_NAME = "testChannelName";
  final String PROFILE_URL = "testProfileUrl";
  final boolean IS_VERIFIED_MARK = false;
  final String CHANNEL_DESCRIPTION = "testChannelDescription";
  final boolean SUBSCRIPTION_AVAILABILITY = true;
  final boolean IS_LIVE = true;
  final int FOLLOWER_COUNT = 1000;

  /**
   * {@code setUp}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2024-03-11 17:02:16 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.2, 근거 커밋 6f9e7fb.
   *
   * @since Ver.0.1.2
   */
  @BeforeAll
  void setUp() {
    final ChzzkChannelEntity chzzkChannelEntity =
        ChzzkChannelEntity.builder()
            .channelId(CHANNEL_ID)
            .channelName(CHANNEL_NAME)
            .profileUrl(PROFILE_URL)
            .isVerifiedMark(IS_VERIFIED_MARK)
            .channelDescription(CHANNEL_DESCRIPTION)
            .subscriptionAvailability(SUBSCRIPTION_AVAILABILITY)
            .isLive(IS_LIVE)
            .followerCount(FOLLOWER_COUNT)
            .lastCheckTime(ZonedDateTime.now())
            .build();

    chzzkChannelRepository.save(chzzkChannelEntity);
  }

  /**
   * {@code saveTest}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2024-03-11 17:02:16 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.2, 근거 커밋 6f9e7fb.
   *
   * @since Ver.0.1.2
   */
  @Test
  @Transactional
  @DisplayName("ChzzkChannelEntity 저장 테스트")
  void saveTest() {
    final String channelId = "testChannelId_2";
    final String channelName = "testChannelName_2";
    final String profileUrl = "testProfileUrl";
    final boolean isVerifiedMark = false;
    final String channelDescription = "testChannelDescription";
    final boolean subscriptionAvailability = true;
    final boolean isLive = true;
    final int followerCount = 1000;

    final ChzzkChannelEntity chzzkChannelEntity =
        ChzzkChannelEntity.builder()
            .channelId(channelId)
            .channelName(channelName)
            .profileUrl(profileUrl)
            .isVerifiedMark(isVerifiedMark)
            .channelDescription(channelDescription)
            .subscriptionAvailability(subscriptionAvailability)
            .isLive(isLive)
            .followerCount(followerCount)
            .lastCheckTime(ZonedDateTime.now())
            .build();

    chzzkChannelRepository.save(chzzkChannelEntity);

    final Optional<ChzzkChannelEntity> savedEntity =
        chzzkChannelRepository.findChzzkChannelEntityByChannelId(channelId);
    assertTrue(savedEntity.isPresent());
    assertEquals(savedEntity.get().getChannelId(), channelId);
    assertEquals(savedEntity.get().getChannelName(), channelName);
    assertEquals(savedEntity.get().getProfileUrl(), profileUrl);
    assertEquals(savedEntity.get().isVerifiedMark(), isVerifiedMark);
    assertEquals(savedEntity.get().getChannelDescription(), channelDescription);
    assertEquals(savedEntity.get().isSubscriptionAvailability(), subscriptionAvailability);
    assertEquals(savedEntity.get().isLive(), isLive);
    assertEquals(savedEntity.get().getFollowerCount(), followerCount);
  }

  /**
   * {@code findChzzkChannelEntityByChannelId_Test}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-03-11 17:02:16 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.2, 근거 커밋 6f9e7fb.
   *
   * @since Ver.0.1.2
   */
  @Test
  @Transactional
  @DisplayName("findChzzkChannelEntityByChannelId 메소드 테스트")
  void findChzzkChannelEntityByChannelId_Test() {
    final Optional<ChzzkChannelEntity> foundEntity =
        chzzkChannelRepository.findChzzkChannelEntityByChannelId(CHANNEL_ID);

    assertTrue(foundEntity.isPresent());
    assertEquals(foundEntity.get().getChannelId(), CHANNEL_ID);
    assertEquals(foundEntity.get().getChannelName(), CHANNEL_NAME);
    assertEquals(foundEntity.get().getProfileUrl(), PROFILE_URL);
    assertEquals(foundEntity.get().isVerifiedMark(), IS_VERIFIED_MARK);
    assertEquals(foundEntity.get().getChannelDescription(), CHANNEL_DESCRIPTION);
    assertEquals(foundEntity.get().isSubscriptionAvailability(), SUBSCRIPTION_AVAILABILITY);
    assertEquals(foundEntity.get().isLive(), IS_LIVE);
    assertEquals(foundEntity.get().getFollowerCount(), FOLLOWER_COUNT);

    final Optional<ChzzkChannelEntity> errorEntity =
        chzzkChannelRepository.findChzzkChannelEntityByChannelId("errorTEST");
    assertTrue(errorEntity.isEmpty());
  }

  /**
   * {@code findChzzkChannelEntityByChannelName_Test}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-03-11 17:02:16 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.2, 근거 커밋 6f9e7fb.
   *
   * @since Ver.0.1.2
   */
  @Test
  @Transactional
  @DisplayName("findChzzkChannelEntityByChannelName 메소드 테스트")
  void findChzzkChannelEntityByChannelName_Test() {
    final Optional<ChzzkChannelEntity> foundEntity =
        chzzkChannelRepository.findChzzkChannelEntityByChannelName(CHANNEL_NAME);

    assertTrue(foundEntity.isPresent());
    assertEquals(foundEntity.get().getChannelId(), CHANNEL_ID);
    assertEquals(foundEntity.get().getChannelName(), CHANNEL_NAME);
    assertEquals(foundEntity.get().getProfileUrl(), PROFILE_URL);
    assertEquals(foundEntity.get().isVerifiedMark(), IS_VERIFIED_MARK);
    assertEquals(foundEntity.get().getChannelDescription(), CHANNEL_DESCRIPTION);
    assertEquals(foundEntity.get().isSubscriptionAvailability(), SUBSCRIPTION_AVAILABILITY);
    assertEquals(foundEntity.get().isLive(), IS_LIVE);
    assertEquals(foundEntity.get().getFollowerCount(), FOLLOWER_COUNT);

    final Optional<ChzzkChannelEntity> errorEntity =
        chzzkChannelRepository.findChzzkChannelEntityByChannelName("errorTEST");
    assertTrue(errorEntity.isEmpty());
  }
}
