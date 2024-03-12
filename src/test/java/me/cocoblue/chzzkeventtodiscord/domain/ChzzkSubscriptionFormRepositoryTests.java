package me.cocoblue.chzzkeventtodiscord.domain;

import jakarta.transaction.Transactional;
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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ChzzkSubscriptionFormRepositoryTests {
    @Autowired
    private ChzzkSubscriptionFormRepository chzzkSubscriptionFormRepository;

    @Autowired
    private DiscordWebhookDataRepository discordWebhookDataRepository;

    @Autowired
    private DiscordBotProfileDataRepository discordBotProfileDataRepository;

    private final ChzzkChannelEntity COMMON_CHANNEL_ENTITY = ChzzkChannelEntity.builder()
            .channelId("123456789")
            .channelName("testChannelName")
            .lastCheckTime(ZonedDateTime.now())
            .build();

    private final DiscordWebhookDataEntity COMMON_WEBHOOK_ENTITY = DiscordWebhookDataEntity.builder()
            .name("testWebhook")
            .meno("This is test memo")
            .webhookUrl("https://discord.com/api/webhooks/1234567890/abcdefghijk")
            .ownerId(COMMON_CHANNEL_ENTITY)
            .build();

    private final DiscordBotProfileDataEntity COMMON_BOT_PROFILE_ENTITY = DiscordBotProfileDataEntity.builder()
            .avatarUrl("https://cocoblue.me/image/test.png")
            .username("testBot")
            .ownerId(COMMON_CHANNEL_ENTITY)
            .build();

    @BeforeAll
    void setUp() {
        discordWebhookDataRepository.save(COMMON_WEBHOOK_ENTITY);
        // save 시, id가 객체에 들어가야함.
        assertNotNull(COMMON_WEBHOOK_ENTITY.getId());

        discordBotProfileDataRepository.save(COMMON_BOT_PROFILE_ENTITY);
        assertNotNull(COMMON_BOT_PROFILE_ENTITY.getId());
        assertEquals(COMMON_BOT_PROFILE_ENTITY.getOwnerId().getChannelId(), COMMON_CHANNEL_ENTITY.getChannelId());
    }

    @Test
    @Transactional
    @DisplayName("findAllByChzzkChannelEntityAndChzzkSubscriptionTypeAndEnabled 메소드 테스트")
    void findAllByChzzkChannelEntityAndChzzkSubscriptionTypeAndEnabled_Test() {
        final List<ChzzkSubscriptionFormEntity> chzzkSubscriptionFormEntityList = new ArrayList<>();
        chzzkSubscriptionFormEntityList.add(ChzzkSubscriptionFormEntity.builder()
                .chzzkChannelEntity(COMMON_CHANNEL_ENTITY)
                .chzzkSubscriptionType(ChzzkSubscriptionType.STREAM_ONLINE)
                .webhookId(COMMON_WEBHOOK_ENTITY)
                .formOwner(COMMON_CHANNEL_ENTITY)
                .languageIsoData(LanguageIsoData.Korean)
                .intervalMinute(10)
                .colorHex("FFFFFF")
                .enabled(true)
                .showDetail(false)
                .content("testContent")
                .botProfileId(COMMON_BOT_PROFILE_ENTITY)
                .build());

        chzzkSubscriptionFormEntityList.add(ChzzkSubscriptionFormEntity.builder()
                .chzzkChannelEntity(COMMON_CHANNEL_ENTITY)
                .chzzkSubscriptionType(ChzzkSubscriptionType.STREAM_ONLINE)
                .webhookId(COMMON_WEBHOOK_ENTITY)
                .formOwner(COMMON_CHANNEL_ENTITY)
                .languageIsoData(LanguageIsoData.Korean)
                .intervalMinute(10)
                .colorHex("FFFFFF")
                .enabled(false)
                .showDetail(false)
                .botProfileId(COMMON_BOT_PROFILE_ENTITY)
                .build());

        chzzkSubscriptionFormRepository.saveAll(chzzkSubscriptionFormEntityList);

        final List<ChzzkSubscriptionFormEntity> result = chzzkSubscriptionFormRepository.findAllByChzzkChannelEntityAndChzzkSubscriptionTypeAndEnabled(COMMON_CHANNEL_ENTITY, ChzzkSubscriptionType.STREAM_ONLINE, true);
        assertEquals(1, result.size());
        assertEquals(chzzkSubscriptionFormEntityList.get(0), result.get(0));

        final List<ChzzkSubscriptionFormEntity> result2 = chzzkSubscriptionFormRepository.findAllByChzzkChannelEntityAndChzzkSubscriptionTypeAndEnabled(COMMON_CHANNEL_ENTITY, ChzzkSubscriptionType.STREAM_ONLINE, false);
        assertEquals(1, result2.size());
        assertEquals(chzzkSubscriptionFormEntityList.get(1), result2.get(0));
    }

}