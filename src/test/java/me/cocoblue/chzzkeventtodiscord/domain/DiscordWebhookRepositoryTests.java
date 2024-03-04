package me.cocoblue.chzzkeventtodiscord.domain;

import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataRepository;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class DiscordWebhookRepositoryTests {
    @Autowired
    private DiscordWebhookDataRepository discordWebhookDataRepository;

    @Autowired
    private ChzzkChannelRepository chzzkChannelRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @BeforeEach
    void setUp() {
        ChzzkChannelEntity chzzkChannelEntity = ChzzkChannelEntity.builder()
                .channelId("testChannelId")
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
    void saveTest() {
        ChzzkChannelEntity chzzkChannelEntity = chzzkChannelRepository.findById("testChannelId").get();

        DiscordWebhookDataEntity entity = DiscordWebhookDataEntity.builder()
                .name("testName")
                .webhookUrl("testWebhookUrl")
                .meno("testMeno")
                .ownerId(chzzkChannelEntity)
                .build();
        DiscordWebhookDataEntity savedEntity = discordWebhookDataRepository.save(entity);
        DiscordWebhookDataEntity foundEntity = testEntityManager.find(DiscordWebhookDataEntity.class, savedEntity.getId());

        assertEquals("testName", foundEntity.getName());
    }
}
