package me.cocoblue.chzzkeventtodiscord.domain;

import jakarta.transaction.Transactional;
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

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ChzzkCategoryRepositoryTests {
    @Autowired
    private ChzzkCategoryRepository chzzkCategoryRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private final String COMMON_CATEGORY_ID = "idNum1";
    private final String COMMON_CATEGORY_TYPE = "GAME";
    private final String COMMON_CATEGORY_NAME = "StarCraft";
    private final String COMMON_CATEGORY_POSTER_URL = "testPosterImageUrl";

    @BeforeAll
    void setUp() {
        final ChzzkCategoryId chzzkCategoryId = ChzzkCategoryId.builder()
                .categoryId(COMMON_CATEGORY_ID)
                .categoryType(COMMON_CATEGORY_TYPE)
                .build();

        final ChzzkCategoryEntity chzzkCategoryEntity = ChzzkCategoryEntity.builder()
                .id(chzzkCategoryId)
                .categoryName(COMMON_CATEGORY_NAME)
                .posterImageUrl(COMMON_CATEGORY_POSTER_URL)
                .updatedAt(ZonedDateTime.now())
                .build();

        chzzkCategoryRepository.save(chzzkCategoryEntity);
    }

    @Test
    @Transactional
    @DisplayName("ChzzkCategoryEntity 저장 테스트")
    void saveTest() {
        final String categoryId = "idNum2";
        final String categoryType = "GAME";
        final String categoryName = "testCategoryName_1";
        final String posterImageUrl = "testPosterImageUrl";

        final ChzzkCategoryId chzzkCategoryId = ChzzkCategoryId.builder()
                .categoryId(categoryId)
                .categoryType(categoryType)
                .build();

        final ChzzkCategoryEntity chzzkCategoryEntity = ChzzkCategoryEntity.builder()
                .id(chzzkCategoryId)
                .categoryName(categoryName)
                .posterImageUrl(posterImageUrl)
                .updatedAt(ZonedDateTime.now())
                .build();

        chzzkCategoryRepository.save(chzzkCategoryEntity);

        final ChzzkCategoryEntity savedEntity = chzzkCategoryRepository.save(chzzkCategoryEntity);
        final ChzzkCategoryEntity foundEntity = testEntityManager.find(ChzzkCategoryEntity.class, savedEntity.getId());

        assertEquals(categoryId, foundEntity.getId().getCategoryId());
        assertEquals(categoryType, foundEntity.getId().getCategoryType());
        assertEquals(categoryName, foundEntity.getCategoryName());
        assertEquals(posterImageUrl, foundEntity.getPosterImageUrl());
    }

    @Test
    @Transactional
    @DisplayName("findByIdCategoryId 메소드 테스트")
    void findByIdCategoryId_Test() {
        final Optional<ChzzkCategoryEntity> foundEntity = chzzkCategoryRepository.findByIdCategoryId(COMMON_CATEGORY_ID);

        assertTrue(foundEntity.isPresent());
        assertEquals(COMMON_CATEGORY_ID, foundEntity.get().getId().getCategoryId());
        assertEquals(COMMON_CATEGORY_TYPE, foundEntity.get().getId().getCategoryType());
        assertEquals(COMMON_CATEGORY_NAME, foundEntity.get().getCategoryName());
        assertEquals(COMMON_CATEGORY_POSTER_URL, foundEntity.get().getPosterImageUrl());

        final Optional<ChzzkCategoryEntity> errorEntity = chzzkCategoryRepository.findByIdCategoryId("errorTEST");
        assertTrue(errorEntity.isEmpty());
    }
}
