package me.cocoblue.chzzkeventtodiscord.domain;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataRepository;
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
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DiscordWebhookRepositoryTests {
    @Autowired
    private DiscordWebhookDataRepository discordWebhookDataRepository;

    @Autowired
    private ChzzkChannelRepository chzzkChannelRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private final String TEST_CHANNEL_ID = "testChannelId";

    @PostConstruct
    void setUp() {
        final ChzzkChannelEntity chzzkChannelEntity = ChzzkChannelEntity.builder()
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

    @Test
    @Transactional
    @DisplayName("DiscordWebhookDataEntity 저장 테스트")
    void saveTest() {
        final Optional<ChzzkChannelEntity> chzzkChannelEntity = chzzkChannelRepository.findById(TEST_CHANNEL_ID);
        assertTrue(chzzkChannelEntity.isPresent());

        final String webhookName = "testName";
        final String webhookUrl = "testWebhookUrl";
        final String meno = "testMeno";

        final DiscordWebhookDataEntity entity = DiscordWebhookDataEntity.builder()
                .name(webhookName)
                .webhookUrl(webhookUrl)
                .meno(meno)
                .ownerId(chzzkChannelEntity.get())
                .build();
        final DiscordWebhookDataEntity savedEntity = discordWebhookDataRepository.save(entity);
        final DiscordWebhookDataEntity foundEntity = testEntityManager.find(DiscordWebhookDataEntity.class, savedEntity.getId());

        assertEquals(webhookName, foundEntity.getName());
        assertEquals(webhookUrl, foundEntity.getWebhookUrl());
        assertEquals(meno, foundEntity.getMeno());
    }

    @Test
    @Transactional
    @DisplayName("findDiscordWebhookDataEntityByWebhookUrlAndNameAndOwnerId 메소드 테스트")
    void findDiscordWebhookDataEntityByWebhookUrlAndNameAndOwnerId_Test() {
        final Optional<ChzzkChannelEntity> chzzkChannelEntity = chzzkChannelRepository.findById(TEST_CHANNEL_ID);
        assertTrue(chzzkChannelEntity.isPresent());

        final String webhookName = "testName";
        final String webhookUrl = "testWebhookUrl";
        final String meno = "testMeno";

        final DiscordWebhookDataEntity entity = DiscordWebhookDataEntity.builder()
                .name(webhookName)
                .webhookUrl(webhookUrl)
                .meno(meno)
                .ownerId(chzzkChannelEntity.get())
                .build();
        discordWebhookDataRepository.save(entity);

        final Optional<DiscordWebhookDataEntity> foundEntity = discordWebhookDataRepository
                .findDiscordWebhookDataEntityByWebhookUrlAndNameAndOwnerId(webhookUrl, webhookName, chzzkChannelEntity.get());

        assertTrue(foundEntity.isPresent());
        assertEquals(webhookName, foundEntity.get().getName());
    }
}
