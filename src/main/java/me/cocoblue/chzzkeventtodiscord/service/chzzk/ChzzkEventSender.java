package me.cocoblue.chzzkeventtodiscord.service.chzzk;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableConditionType;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableGroupType;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkSubscriptionType;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;
import me.cocoblue.chzzkeventtodiscord.domain.eventlog.NotificationLogRepository;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkCategoryDTO;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkChannelDTO;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkLiveDTO;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkLiveStatusDTO;
import me.cocoblue.chzzkeventtodiscord.dto.discord.DiscordEmbed;
import me.cocoblue.chzzkeventtodiscord.service.ChzzkSubscriptionFormService;
import me.cocoblue.chzzkeventtodiscord.service.DiscordWebhookService;
import me.cocoblue.chzzkeventtodiscord.service.NotificationLogService;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Log4j2
@Service
@RequiredArgsConstructor
public class ChzzkEventSender {
    private final DiscordWebhookService discordWebhookService;
    private final ChzzkLiveStatusService liveStatusService;
    private final NotificationLogService notificationLogService;
    private final ChzzkSubscriptionFormService subscriptionFormService;
    private final NotificationLogRepository notificationLogRepository;
    private final ChzzkCategoryService categoryService;
    private final MessageSource messageSource;
    private final String CHZZK_URL = "https://chzzk.naver.com";
    private final String CHZZK_FAVICON_URL = "https://chzzk.naver.com/favicon.ico";

    @Async
    public void sendEvent(final ChzzkChannelDTO chzzkChannelDTO, final ChzzkSubscriptionType subscriptionType) {
        log.info("Send event information to Discord. channelId: {}", chzzkChannelDTO.getChannelId());
        final ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));

        // 기존 데이터 로드
        final List<ChzzkSubscriptionFormEntity> filteredForms =
                subscriptionFormService.findAllByChannelEntityAndSubscriptionTypeAndEnabled(chzzkChannelDTO.getChannelId(), subscriptionType, true)
                        .stream()
                        .filter(form -> {
                            int count = notificationLogRepository.getCountBySubscriptionFormAndCreatedAtBetween(form, now.minusMinutes(form.getIntervalMinute()), now);
                            return count == 0;
                        })
                        .toList();

        if(filteredForms.isEmpty()) {
            log.info("No subscription form for event. channelId: {}", chzzkChannelDTO.getChannelId());
            return;
        }

        if (subscriptionType == ChzzkSubscriptionType.STREAM_OFFLINE) {
            filteredForms.forEach(form -> processStreamOfflineEvent(form, chzzkChannelDTO));
            return;
        }

        final ChzzkLiveDTO chzzkLiveDTO = liveStatusService.getLiveStatusFromSearchAPI(chzzkChannelDTO.getChannelName());
        final ChzzkLiveStatusDTO liveStatus = liveStatusService.getLiveStatusFromAPI(chzzkChannelDTO.getChannelId());

        if(chzzkLiveDTO.getLiveCategory() == null || chzzkLiveDTO.getCategoryType() == null || chzzkLiveDTO.getLiveCategoryValue() == null) {
            log.warn("Category Type or Category Id is NULL. It may cause channel owner isn't set the category.");
        }

        final ChzzkCategoryDTO categoryData = categoryService.getCategoryInfo(chzzkLiveDTO.getCategoryType(), chzzkLiveDTO.getLiveCategory());

        if (subscriptionType == ChzzkSubscriptionType.STREAM_ONLINE) {
            filteredForms.forEach(form -> processStreamOnlineEvent(form, chzzkChannelDTO, liveStatus, chzzkLiveDTO, categoryData));
        }
    }

    @Async
    public void processStreamOfflineEvent(final ChzzkSubscriptionFormEntity form, final ChzzkChannelDTO channelData) {
        final DiscordEmbed.Webhook webhook = makeStreamOfflineDiscordWebhook(form, channelData);
        discordWebhookService.sendDiscordWebhook(webhook, form.getWebhookId().getWebhookUrl());
        notificationLogService.insertNotificationLog(form.getId());
    }

    @Async
    public void processStreamOnlineEvent(final ChzzkSubscriptionFormEntity form, final ChzzkChannelDTO channelData,
                                         final ChzzkLiveStatusDTO liveStatus, final ChzzkLiveDTO chzzkLiveDTO,
                                         final ChzzkCategoryDTO categoryData) {
        final DiscordEmbed.Webhook webhook = makeStreamOnlineDiscordWebhook(form, channelData, liveStatus, chzzkLiveDTO, categoryData);
        discordWebhookService.sendDiscordWebhook(webhook, form.getWebhookId().getWebhookUrl());
        notificationLogService.insertNotificationLog(form.getId());
    }

    private DiscordEmbed.Webhook makeStreamOnlineDiscordWebhook(final ChzzkSubscriptionFormEntity form, final ChzzkChannelDTO channelData,
                                                                final ChzzkLiveStatusDTO liveStatus, final ChzzkLiveDTO chzzkLiveDTO,
                                                                final ChzzkCategoryDTO categoryData) {
        // Form의 Locale 얻기
        final Locale locale = Locale.forLanguageTag(form.getLanguageIsoData().getCode());

        // Author Area
        // https://chzzk.naver.com/live/<Channel ID>
        final String liveURL = String.format("%s/live/%s", CHZZK_URL, channelData.getChannelId());

        // Thumbnail
        DiscordEmbed.Thumbnail thumbnail = null;
        if(categoryData == null) {
            log.warn("Category Type or Category Id is NULL. It may cause channel owner isn't set the category.");
        } else {
            final String thumbnailUrl = categoryData.getPosterImageUrl();
            thumbnail = DiscordEmbed.Thumbnail.builder().url(thumbnailUrl).build();
        }

        // Embed Area
        final String embedColor = Integer.toString(form.getDecimalColor());
        final String categoryName = categoryData == null ? null : categoryData.getCategoryValue();
        final String embedDescription = categoryData == null ? messageSource.getMessage("game.none", null, locale) : messageSource.getMessage("game.prefix", null, locale) + categoryName;
        final String embedTitle = chzzkLiveDTO.getLiveTitle();

        // Embed Field Area
        final List<DiscordEmbed.Field> fields = new ArrayList<>();

        // Embed Footer Area
        final DiscordEmbed.Footer footer = new DiscordEmbed.Footer(messageSource.getMessage("stream.online.footer", null, locale), CHZZK_FAVICON_URL);

        // Embed Timestamp -> KST로 오기 때문에 UTC로 변환.
        final LocalDateTime startTime = chzzkLiveDTO.getOpenDate().minusHours(9);

        // 채팅에 특정 조건이 걸려있으면 공지
        if(liveStatus.getChatAvailableGroup() != ChzzkChatAvailableGroupType.ALL) {
            final DiscordEmbed.Field chatAvailableGroupField = DiscordEmbed.Field.builder()
                    .name(messageSource.getMessage("stream.online.chat-available-group", null, locale))
                    .value(messageSource.getMessage(liveStatus.getChatAvailableGroup().getStringKey(), null, locale))
                    .inline(true)
                    .build();

            fields.add(chatAvailableGroupField);
        }

        // 채팅에 계정 조건이 걸려있으면 공지
        if(liveStatus.getChatAvailableCondition() != ChzzkChatAvailableConditionType.NONE) {
            final DiscordEmbed.Field chatAvailableGroupField = DiscordEmbed.Field.builder()
                    .name(messageSource.getMessage("stream.online.chat-available-condition", null, locale))
                    .value(messageSource.getMessage(liveStatus.getChatAvailableCondition().getStringKey(), null, locale))
                    .inline(true)
                    .build();

            fields.add(chatAvailableGroupField);
        }

        final DiscordEmbed.Author author = createAuthor(channelData, "stream.online.event-message", locale, ChzzkSubscriptionType.STREAM_ONLINE);

        final DiscordEmbed.Image image = DiscordEmbed.Image.builder()
                .url(chzzkLiveDTO.getLiveImageUrl().replace("{type}", "480"))
                .build();

        final List<DiscordEmbed> discordEmbeds = new ArrayList<>();
        final DiscordEmbed discordEmbed = createDiscordEmbed(author, embedTitle, liveURL, embedDescription, embedColor, fields, footer, String.valueOf(startTime), thumbnail, image);
        discordEmbeds.add(discordEmbed);

        return new DiscordEmbed.Webhook(form.getBotProfileId().getUsername(),
                form.getBotProfileId().getAvatarUrl(), form.getContent(), discordEmbeds);
    }

    private DiscordEmbed.Webhook makeStreamOfflineDiscordWebhook(final ChzzkSubscriptionFormEntity form, final ChzzkChannelDTO channelData) {
        // Form의 Locale 얻기
        final Locale locale = Locale.forLanguageTag(form.getLanguageIsoData().getCode());

        // Author Area
        // https://chzzk.naver.com/<Channel ID>
        final String liveURL = String.format("%s/%s", CHZZK_URL, channelData.getChannelId());

        // Embed Area
        final String embedColor = Integer.toString(form.getDecimalColor());
        final String embedDescription = messageSource.getMessage("stream.offline.embed-description", null, locale);
        final String embedTitle = messageSource.getMessage("stream.offline.embed-title", null, locale);

        // Embed Field Area
        final List<DiscordEmbed.Field> fields = new ArrayList<>();

        // Embed Footer Area
        final DiscordEmbed.Footer footer = new DiscordEmbed.Footer(messageSource.getMessage("stream.offline.footer", null, locale), CHZZK_FAVICON_URL);

        // Embed Timestamp -> KST로 오기 때문에 UTC로 변환.
        final LocalDateTime submitTime = ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime();

        final DiscordEmbed.Author author = createAuthor(channelData, "stream.offline.event-message", locale, ChzzkSubscriptionType.STREAM_OFFLINE);

        final List<DiscordEmbed> discordEmbeds = new ArrayList<>();
        final DiscordEmbed discordEmbed = createDiscordEmbed(author, embedTitle, liveURL, embedDescription, embedColor, fields, footer, String.valueOf(submitTime), null, null);
        discordEmbeds.add(discordEmbed);

        return new DiscordEmbed.Webhook(form.getBotProfileId().getUsername(),
                form.getBotProfileId().getAvatarUrl(), form.getContent(), discordEmbeds);
    }

    private DiscordEmbed.Author createAuthor(final ChzzkChannelDTO channelData, final String messageKey,
                                             final Locale locale, final ChzzkSubscriptionType subscriptionType) {
        String liveURL = String.format("%s/%s", CHZZK_URL, channelData.getChannelId());;
        if(subscriptionType == ChzzkSubscriptionType.STREAM_ONLINE) {
            liveURL = String.format("%s/live/%s", CHZZK_URL, channelData.getChannelId());
        }

        final String authorProfileURL = channelData.getChannelImageUrl();
        final String authorName = messageSource.getMessage(messageKey, new Object[]{channelData.getChannelName()}, locale);

        return new DiscordEmbed.Author(authorName, liveURL, authorProfileURL);
    }

    private DiscordEmbed createDiscordEmbed(final DiscordEmbed.Author author, final String embedTitle, final String embedUrl, final String embedDescription,
                                            final String embedColor, final List<DiscordEmbed.Field> fields, final DiscordEmbed.Footer footer,
                                            final String timestamp, final DiscordEmbed.Thumbnail thumbnail, final DiscordEmbed.Image image) {
        return DiscordEmbed.builder()
                .author(author)
                .title(embedTitle)
                .url(embedUrl)
                .description(embedDescription)
                .color(embedColor)
                .fields(fields)
                .footer(footer)
                .timestamp(timestamp)
                .thumbnail(thumbnail)
                .image(image)
                .build();
    }
}
