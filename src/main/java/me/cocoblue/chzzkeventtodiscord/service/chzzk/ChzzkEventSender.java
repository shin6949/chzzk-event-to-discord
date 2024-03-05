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
    private final String CHZZK_FAVICON_URL = "https://play-lh.googleusercontent.com/wvo3IB5dTJHyjpIHvkdzpgbFnG3LoVsqKdQ7W3IoRm-EVzISMz9tTaIYoRdZm1phL_8=w120-h120-rw";

    @Async
    public void sendStreamEvent(final ChzzkChannelDTO chzzkChannelDTO, final ChzzkSubscriptionType subscriptionType) {
        log.info("Send stream related event information to Discord. channelId: {}", chzzkChannelDTO.getChannelId());
        final ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));

        // 기존 데이터 로드
        final List<ChzzkSubscriptionFormEntity> filteredForms =
                subscriptionFormService.findAllByChannelEntityAndSubscriptionTypeAndEnabled(chzzkChannelDTO.getChannelId(), subscriptionType, true)
                        .stream()
                        .filter(form -> {
                            int count = notificationLogRepository.getCountBySubscriptionFormAndCreatedAtBetween(form, now.minusMinutes(form.getIntervalMinute()), now).size();
                            return count == 0;
                        })
                        .toList();

        if (filteredForms.isEmpty()) {
            log.info("No subscription form for event. channelId: {}", chzzkChannelDTO.getChannelId());
            return;
        }

        if (subscriptionType == ChzzkSubscriptionType.STREAM_OFFLINE) {
            filteredForms.forEach(form -> processStreamOfflineEvent(form, chzzkChannelDTO));
            return;
        }

        final ChzzkLiveDTO chzzkLiveDTO = liveStatusService.getLiveStatusFromSearchAPI(chzzkChannelDTO.getChannelName());
        final ChzzkLiveStatusDTO liveStatus = liveStatusService.getLiveStatusFromAPI(chzzkChannelDTO.getChannelId());
        log.info("liveStatus: {}", liveStatus);

        ChzzkCategoryDTO categoryData;
        if (liveStatus.getCategoryId() == null || liveStatus.getCategoryType() == null || liveStatus.getCategoryValue() == null) {
            categoryData = null;
            log.warn("Category Type or Category Id is NULL. It may cause channel owner isn't set the category.");
        } else {
            categoryData = categoryService.getCategoryInfo(liveStatus.getCategoryType(), liveStatus.getCategoryId());
        }

        if (subscriptionType == ChzzkSubscriptionType.STREAM_ONLINE) {
            filteredForms.forEach(form -> processStreamOnlineEvent(form, chzzkChannelDTO, liveStatus, chzzkLiveDTO, categoryData));
        }
    }

    @Async
    public void sendChannelUpdateEvent(final ChzzkChannelDTO afterChannelData) {
        log.info("Send channel event information to Discord. channelId: {}", afterChannelData.getChannelId());
        final ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));

        // 기존 데이터 로드
        final List<ChzzkSubscriptionFormEntity> filteredForms =
                subscriptionFormService.findAllByChannelEntityAndSubscriptionTypeAndEnabled(afterChannelData.getChannelId(), ChzzkSubscriptionType.CHANNEL_UPDATE, true)
                        .stream()
                        .filter(form -> {
                            log.info("log count: {}", notificationLogRepository.getCountBySubscriptionFormAndCreatedAtBetween(form, now.minusMinutes(form.getIntervalMinute()), now).size());
                            int count = notificationLogRepository.getCountBySubscriptionFormAndCreatedAtBetween(form, now.minusMinutes(form.getIntervalMinute()), now).size();
                            return count == 0;
                        })
                        .toList();

        if (filteredForms.isEmpty()) {
            log.info("No subscription form for event. channelId: {}", afterChannelData.getChannelId());
            return;
        }

        filteredForms.forEach(form -> processChannelUpdateEvent(form, afterChannelData));
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

    @Async
    public void processChannelUpdateEvent(final ChzzkSubscriptionFormEntity form, final ChzzkChannelDTO afterChannelData) {
        final DiscordEmbed.Webhook webhook = makeChannelUpdateDiscordWebhook(form, afterChannelData);
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
        if (categoryData == null) {
            log.warn("Category Type or Category Id is NULL. It may cause channel owner isn't set the category.");
        } else {
            final String thumbnailUrl = categoryData.getPosterImageUrl();
            thumbnail = DiscordEmbed.Thumbnail.builder().url(thumbnailUrl).build();
        }

        // Embed Area
        final String embedColor = Integer.toString(form.getDecimalColor());
        final String categoryName = categoryData == null ? null : categoryData.getCategoryValue();
        final String embedDescription = categoryData == null ? messageSource.getMessage("game.none", null, locale) : messageSource.getMessage("game.prefix", null, locale) + categoryName;
        final String embedTitle = liveStatus.getLiveTitle();

        // Embed Field Area
        final List<DiscordEmbed.Field> fields = new ArrayList<>();

        // Embed Footer Area
        final DiscordEmbed.Footer footer = new DiscordEmbed.Footer(messageSource.getMessage("stream.online.footer", null, locale), CHZZK_FAVICON_URL);

        // Embed Timestamp -> KST로 오기 때문에 UTC로 변환.
        final LocalDateTime startTime = chzzkLiveDTO == null ? ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime() :
                chzzkLiveDTO.getOpenDate().minusHours(9);

        // 상세 정보를 Field에 보여줄지 여부
        if (form.isShowDetail()) {
            // 채팅에 특정 조건이 걸려있으면 공지
            if (liveStatus.getChatAvailableGroup() != ChzzkChatAvailableGroupType.ALL) {
                final DiscordEmbed.Field chatAvailableGroupField = DiscordEmbed.Field.builder()
                        .name(messageSource.getMessage("stream.online.chat-available-group", null, locale))
                        .value(messageSource.getMessage(liveStatus.getChatAvailableGroup().getStringKey(), null, locale))
                        .inline(true)
                        .build();

                fields.add(chatAvailableGroupField);
            }

            // 채팅에 계정 조건이 걸려있으면 공지
            if (liveStatus.getChatAvailableCondition() != ChzzkChatAvailableConditionType.NONE) {
                final DiscordEmbed.Field chatAvailableGroupField = DiscordEmbed.Field.builder()
                        .name(messageSource.getMessage("stream.online.chat-available-condition", null, locale))
                        .value(messageSource.getMessage(liveStatus.getChatAvailableCondition().getStringKey(), null, locale))
                        .inline(true)
                        .build();

                fields.add(chatAvailableGroupField);
            }

            // 채팅에 성인인증 조건이 걸려있으면 공지
            if (liveStatus.isAdult()) {
                final DiscordEmbed.Field isAdultField = DiscordEmbed.Field.builder()
                        .name(messageSource.getMessage("stream.online.adult", null, locale))
                        .value(messageSource.getMessage("stream.online.true", null, locale))
                        .inline(true)
                        .build();

                fields.add(isAdultField);
            }
        }

        final DiscordEmbed.Author author = createAuthor(channelData, "stream.online.event-message", locale, ChzzkSubscriptionType.STREAM_ONLINE);

        final DiscordEmbed.Image image = chzzkLiveDTO == null ? null : DiscordEmbed.Image.builder()
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

    private DiscordEmbed.Webhook makeChannelUpdateDiscordWebhook(final ChzzkSubscriptionFormEntity form, final ChzzkChannelDTO afterChannelData) {
        // Form의 Locale 얻기
        final Locale locale = Locale.forLanguageTag(form.getLanguageIsoData().getCode());

        // Author Area
        // https://chzzk.naver.com/<Channel ID>
        final String liveURL = String.format("%s/%s", CHZZK_URL, afterChannelData.getChannelId());

        // Embed Area
        final String embedColor = Integer.toString(form.getDecimalColor());
        final String embedDescription = messageSource.getMessage("channel.update.embed-description", null, locale);
        final String embedTitle = messageSource.getMessage("channel.update.embed-title", null, locale);

        // Embed Field Area
        final List<DiscordEmbed.Field> fields = new ArrayList<>();
        fields.add(DiscordEmbed.Field.builder()
                .name(messageSource.getMessage("channel.update.name", null, locale))
                .value(afterChannelData.getChannelName())
                .inline(true)
                .build());

        fields.add(DiscordEmbed.Field.builder()
                .name(messageSource.getMessage("channel.update.description", null, locale))
                .value(afterChannelData.getChannelDescription())
                .inline(false)
                .build());

        fields.add(DiscordEmbed.Field.builder()
                .name(messageSource.getMessage("channel.update.follower", null, locale))
                .value(messageSource.getMessage("channel.update.follower-count", new Object[]{afterChannelData.getFollowerCount()}, locale))
                .inline(true)
                .build());

        fields.add(DiscordEmbed.Field.builder()
                .name(messageSource.getMessage("channel.update.verified", null, locale))
                .value(afterChannelData.getVerifiedMark() ? messageSource.getMessage("channel.update.true", null, locale) :
                        messageSource.getMessage("channel.update.false", null, locale))
                .inline(true)
                .build());

        fields.add(DiscordEmbed.Field.builder()
                .name(messageSource.getMessage("channel.update.open-live", null, locale))
                .value(afterChannelData.isOpenLive() ? messageSource.getMessage("channel.update.true", null, locale) :
                        messageSource.getMessage("channel.update.false", null, locale))
                .inline(true)
                .build());

        fields.add(DiscordEmbed.Field.builder()
                .name(messageSource.getMessage("channel.update.subscription-available", null, locale))
                .value(afterChannelData.isSubscriptionAvailability() ? messageSource.getMessage("channel.update.true", null, locale) :
                        messageSource.getMessage("channel.update.false", null, locale))
                .inline(true)
                .build());

        // Embed Footer Area
        final DiscordEmbed.Footer footer = new DiscordEmbed.Footer(messageSource.getMessage("channel.update.footer", null, locale), CHZZK_FAVICON_URL);

        // Embed Timestamp -> KST로 오기 때문에 UTC로 변환.
        final LocalDateTime submitTime = ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime();

        final DiscordEmbed.Author author = createAuthor(afterChannelData, "channel.update.event-message", locale, ChzzkSubscriptionType.CHANNEL_UPDATE);

        final List<DiscordEmbed> discordEmbeds = new ArrayList<>();
        final DiscordEmbed discordEmbed = createDiscordEmbed(author, embedTitle, liveURL, embedDescription, embedColor, fields, footer, String.valueOf(submitTime), null, null);
        discordEmbeds.add(discordEmbed);

        return new DiscordEmbed.Webhook(form.getBotProfileId().getUsername(),
                form.getBotProfileId().getAvatarUrl(), form.getContent(), discordEmbeds);
    }

    private DiscordEmbed.Author createAuthor(final ChzzkChannelDTO channelData, final String messageKey,
                                             final Locale locale, final ChzzkSubscriptionType subscriptionType) {
        String liveURL = String.format("%s/%s", CHZZK_URL, channelData.getChannelId());

        if (subscriptionType == ChzzkSubscriptionType.STREAM_ONLINE) {
            liveURL = String.format("%s/live/%s", CHZZK_URL, channelData.getChannelId());
        }

        final String authorProfileURL = channelData.getChannelImageUrl() == null ? null : channelData.getChannelImageUrl() + "?type=f120_120_na";
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
