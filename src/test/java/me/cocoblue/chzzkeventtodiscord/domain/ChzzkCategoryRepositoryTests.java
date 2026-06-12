package me.cocoblue.chzzkeventtodiscord.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.Optional;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkCategoryEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkCategoryId;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkCategoryRepository;
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
 * {@code ChzzkCategoryRepositoryTests}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2024-03-09 22:32:50 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.2, 근거 커밋 04ac179.
 *
 * @since Ver.0.1.2
 */
@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ChzzkCategoryRepositoryTests {
  @Autowired private ChzzkCategoryRepository chzzkCategoryRepository;

  @Autowired private TestEntityManager testEntityManager;

  private final String COMMON_CATEGORY_ID = "idNum1";
  private final String COMMON_CATEGORY_TYPE = "GAME";
  private final String COMMON_CATEGORY_NAME = "StarCraft";
  private final String COMMON_CATEGORY_POSTER_URL = "testPosterImageUrl";

  /**
   * {@code setUp}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2024-03-09 22:32:50 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.2, 근거 커밋 04ac179.
   *
   * @since Ver.0.1.2
   */
  @BeforeAll
  void setUp() {
    final ChzzkCategoryId chzzkCategoryId =
        ChzzkCategoryId.builder()
            .categoryId(COMMON_CATEGORY_ID)
            .categoryType(COMMON_CATEGORY_TYPE)
            .build();

    final ChzzkCategoryEntity chzzkCategoryEntity =
        ChzzkCategoryEntity.builder()
            .id(chzzkCategoryId)
            .categoryName(COMMON_CATEGORY_NAME)
            .posterImageUrl(COMMON_CATEGORY_POSTER_URL)
            .updatedAt(ZonedDateTime.now())
            .build();

    chzzkCategoryRepository.save(chzzkCategoryEntity);
  }

  /**
   * {@code saveTest}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2024-03-09 22:32:50 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.2, 근거 커밋 04ac179.
   *
   * @since Ver.0.1.2
   */
  @Test
  @Transactional
  @DisplayName("ChzzkCategoryEntity 저장 테스트")
  void saveTest() {
    final String categoryId = "idNum2";
    final String categoryType = "GAME";
    final String categoryName = "testCategoryName_1";
    final String posterImageUrl = "testPosterImageUrl";

    final ChzzkCategoryId chzzkCategoryId =
        ChzzkCategoryId.builder().categoryId(categoryId).categoryType(categoryType).build();

    final ChzzkCategoryEntity chzzkCategoryEntity =
        ChzzkCategoryEntity.builder()
            .id(chzzkCategoryId)
            .categoryName(categoryName)
            .posterImageUrl(posterImageUrl)
            .updatedAt(ZonedDateTime.now())
            .build();

    chzzkCategoryRepository.save(chzzkCategoryEntity);

    final ChzzkCategoryEntity savedEntity = chzzkCategoryRepository.save(chzzkCategoryEntity);
    final ChzzkCategoryEntity foundEntity =
        testEntityManager.find(ChzzkCategoryEntity.class, savedEntity.getId());

    assertEquals(categoryId, foundEntity.getId().getCategoryId());
    assertEquals(categoryType, foundEntity.getId().getCategoryType());
    assertEquals(categoryName, foundEntity.getCategoryName());
    assertEquals(posterImageUrl, foundEntity.getPosterImageUrl());
  }

  /**
   * {@code findByIdCategoryId_Test}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-03-11 17:02:16 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.2, 근거 커밋 6f9e7fb.
   *
   * @since Ver.0.1.2
   */
  @Test
  @Transactional
  @DisplayName("findByIdCategoryId 메소드 테스트")
  void findByIdCategoryId_Test() {
    final Optional<ChzzkCategoryEntity> foundEntity =
        chzzkCategoryRepository.findByIdCategoryId(COMMON_CATEGORY_ID);

    assertTrue(foundEntity.isPresent());
    assertEquals(COMMON_CATEGORY_ID, foundEntity.get().getId().getCategoryId());
    assertEquals(COMMON_CATEGORY_TYPE, foundEntity.get().getId().getCategoryType());
    assertEquals(COMMON_CATEGORY_NAME, foundEntity.get().getCategoryName());
    assertEquals(COMMON_CATEGORY_POSTER_URL, foundEntity.get().getPosterImageUrl());

    final Optional<ChzzkCategoryEntity> errorEntity =
        chzzkCategoryRepository.findByIdCategoryId("errorTEST");
    assertTrue(errorEntity.isEmpty());
  }
}
