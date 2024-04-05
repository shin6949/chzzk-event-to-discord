package me.cocoblue.chzzkeventtodiscord.service.chzzk;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableConditionType;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableGroupType;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkSubscriptionType;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkStreamOnlineFormEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;
import me.cocoblue.chzzkeventtodiscord.domain.eventlog.NotificationLogRepository;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkCategoryDto;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkChannelDto;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkLiveDetailDto;
import me.cocoblue.chzzkeventtodiscord.dto.discord.DiscordEmbed;
import me.cocoblue.chzzkeventtodiscord.service.ChzzkStreamOnlineFormService;
import me.cocoblue.chzzkeventtodiscord.service.ChzzkSubscriptionFormService;
import me.cocoblue.chzzkeventtodiscord.service.DiscordWebhookService;
import me.cocoblue.chzzkeventtodiscord.service.NotificationLogService;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class ChzzkEventSender {
    private final DiscordWebhookService discordWebhookService;
    private final ChzzkLiveDetailService liveStatusService;
    private final NotificationLogService notificationLogService;
    private final ChzzkSubscriptionFormService subscriptionFormService;
    private final ChzzkStreamOnlineFormService streamOnlineFormService;
    private final NotificationLogRepository notificationLogRepository;
    private final ChzzkCategoryService categoryService;
    private final MessageSource messageSource;
    private final String chzzkUrl = "https://chzzk.naver.com";
    private final String chzzkFaviconUrl = "https://play-lh.googleusercontent.com/wvo3IB5dTJHyjpIHvkdzpgbFnG3LoVsqKdQ7W3IoRm-EVzISMz9tTaIYoRdZm1phL_8=w120-h120-rw";

    @Async
    public void sendStreamOnlineEvent(final ChzzkChannelDto chzzkChannelDto, final ChzzkSubscriptionType subscriptionType) {
        log.info("Send stream related event information to Discord. channelId: {}", chzzkChannelDto.getChannelId());
        final ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));

        // 기존 데이터 로드
        final List<ChzzkStreamOnlineFormEntity> filteredForms =
                streamOnlineFormService.findAllByChannelEntityAndSubscriptionTypeAndEnabled(chzzkChannelDto.getChannelId(), subscriptionType, true)
                        .stream()
                        .filter(form -> {
                            int count = notificationLogRepository.getCountBySubscriptionFormAndCreatedAtBetween(form, now.minusMinutes(form.getIntervalMinute()), now).size();
                            return count == 0;
                        })
                        .toList();

        if (filteredForms.isEmpty()) {
            log.info("No subscription form for event. channelId: {}", chzzkChannelDto.getChannelId());
            return;
        }

        final ChzzkLiveDetailDto chzzkLiveDetailDto = liveStatusService.getLiveDetailFromApi(chzzkChannelDto.getChannelId());
        log.debug("liveDetail: {}", chzzkLiveDetailDto);
        final ChzzkCategoryDto categoryData;
        if (chzzkLiveDetailDto.getCategoryId() == null || chzzkLiveDetailDto.getCategoryType() == null || chzzkLiveDetailDto.getCategoryValue() == null) {
            categoryData = null;
            log.warn("Category Type or Category Id is NULL. It may cause channel owner isn't set the category.");
        } else {
            categoryData = categoryService.getCategoryInfo(chzzkLiveDetailDto.getCategoryType(), chzzkLiveDetailDto.getCategoryId());
        }

        if (subscriptionType == ChzzkSubscriptionType.STREAM_ONLINE) {
            filteredForms.forEach(form -> processStreamOnlineEvent(form, chzzkChannelDto, chzzkLiveDetailDto, categoryData));
        }
    }

    @Async
    public void sendStreamOfflineEvent(final ChzzkChannelDto chzzkChannelDto, final ChzzkSubscriptionType subscriptionType) {
        log.info("Send stream related event information to Discord. channelId: {}", chzzkChannelDto.getChannelId());
        final ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));

        // 기존 데이터 로드
        final List<ChzzkSubscriptionFormEntity> filteredForms =
                subscriptionFormService.findAllByChannelEntityAndSubscriptionTypeAndEnabled(chzzkChannelDto.getChannelId(), subscriptionType, true)
                        .stream()
                        .filter(form -> {
                            int count = notificationLogRepository.getCountBySubscriptionFormAndCreatedAtBetween(form, now.minusMinutes(form.getIntervalMinute()), now).size();
                            return count == 0;
                        })
                        .toList();

        if (filteredForms.isEmpty()) {
            log.info("No subscription form for event. channelId: {}", chzzkChannelDto.getChannelId());
            return;
        }

        filteredForms.forEach(form -> processStreamOfflineEvent(form, chzzkChannelDto));
    }


    @Async
    public void sendChannelUpdateEvent(final ChzzkChannelDto afterChannelData) {
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
    public void processStreamOfflineEvent(final ChzzkSubscriptionFormEntity form, final ChzzkChannelDto channelData) {
        final DiscordEmbed.Webhook webhook = makeStreamOfflineDiscordWebhook(form, channelData);
        discordWebhookService.sendDiscordWebhook(webhook, form.getWebhookId().getWebhookUrl());
        notificationLogService.insertNotificationLog(form.getId());
    }

    @Async
    public void processStreamOnlineEvent(final ChzzkStreamOnlineFormEntity form, final ChzzkChannelDto channelData,
                                         final ChzzkLiveDetailDto liveDetailDto,
                                         final ChzzkCategoryDto categoryData) {
        final DiscordEmbed.Webhook webhook = makeStreamOnlineDiscordWebhook(form, channelData, liveDetailDto, categoryData);
        discordWebhookService.sendDiscordWebhook(webhook, form.getWebhookId().getWebhookUrl());
        notificationLogService.insertNotificationLog(form.getId());
    }

    @Async
    public void processChannelUpdateEvent(final ChzzkSubscriptionFormEntity form, final ChzzkChannelDto afterChannelData) {
        final DiscordEmbed.Webhook webhook = makeChannelUpdateDiscordWebhook(form, afterChannelData);
        discordWebhookService.sendDiscordWebhook(webhook, form.getWebhookId().getWebhookUrl());
        notificationLogService.insertNotificationLog(form.getId());
    }

    private DiscordEmbed.Webhook makeStreamOnlineDiscordWebhook(final ChzzkStreamOnlineFormEntity form, final ChzzkChannelDto channelData,
                                                                final ChzzkLiveDetailDto liveDetailDto,
                                                                final ChzzkCategoryDto categoryData) {
        // Form의 Locale 얻기
        final Locale locale = Locale.forLanguageTag(form.getLanguageIsoData().getCode());

        // Author Area
        // https://chzzk.naver.com/live/<Channel ID>
        final String liveURL = String.format("%s/live/%s", chzzkUrl, channelData.getChannelId());

        // Thumbnail
        DiscordEmbed.Thumbnail thumbnail = null;
        if (categoryData == null || categoryData.getPosterImageUrl() == null) {
            log.warn("Category Type or Category Id is NULL. It may cause channel owner isn't set the category.");
        } else {
            final String thumbnailUrl = categoryData.getPosterImageUrl();
            thumbnail = DiscordEmbed.Thumbnail.builder().url(thumbnailUrl).build();
        }

        // Embed Area
        final String embedColor = Integer.toString(form.getDecimalColor());
        final String categoryName = categoryData == null ? null : categoryData.getCategoryValue();
        final String embedDescription = categoryData == null ? messageSource.getMessage("game.none", null, locale) : messageSource.getMessage("game.prefix", null, locale) + categoryName;
        final String embedTitle = liveDetailDto.getLiveTitle();

        // Embed Field Area
        final List<DiscordEmbed.Field> fields = new ArrayList<>();

        // Embed Footer Area
        final DiscordEmbed.Footer footer = new DiscordEmbed.Footer(messageSource.getMessage("stream.online.footer", null, locale), chzzkFaviconUrl);

        // Embed Timestamp -> KST로 오기 때문에 UTC로 변환.
        final LocalDateTime startTime = liveDetailDto.getOpenDate().minusHours(9);

        // 상세 정보를 Field에 보여줄지 여부
        if (form.isShowDetail()) {
            // 채팅에 특정 조건이 걸려있으면 공지
            if (liveDetailDto.getChatAvailableGroup() != ChzzkChatAvailableGroupType.ALL) {
                final DiscordEmbed.Field chatAvailableGroupField = DiscordEmbed.Field.builder()
                        .name(messageSource.getMessage("stream.online.chat-available-group", null, locale))
                        .value(messageSource.getMessage(liveDetailDto.getChatAvailableGroup().getStringKey(), null, locale))
                        .inline(true)
                        .build();

                fields.add(chatAvailableGroupField);
            }

            // 채팅에 계정 조건이 걸려있으면 공지
            if (liveDetailDto.getChatAvailableCondition() != ChzzkChatAvailableConditionType.NONE) {
                final DiscordEmbed.Field chatAvailableGroupField = DiscordEmbed.Field.builder()
                        .name(messageSource.getMessage("stream.online.chat-available-condition", null, locale))
                        .value(messageSource.getMessage(liveDetailDto.getChatAvailableCondition().getStringKey(), null, locale))
                        .inline(true)
                        .build();

                fields.add(chatAvailableGroupField);
            }

            // 채팅에 성인인증 조건이 걸려있으면 공지
            if (liveDetailDto.isAdult()) {
                final DiscordEmbed.Field isAdultField = DiscordEmbed.Field.builder()
                        .name(messageSource.getMessage("stream.online.adult", null, locale))
                        .value(messageSource.getMessage("stream.online.true", null, locale))
                        .inline(true)
                        .build();

                fields.add(isAdultField);
            }
        }

        // 시청자 수를 Field에 보여줄지 여부
        if(form.isShowViewerCount()) {
            final DiscordEmbed.Field viewerCountField = DiscordEmbed.Field.builder()
                    .name(messageSource.getMessage("stream.online.viewer-count", null, locale))
                    .value(messageSource.getMessage("stream.online.viewer-count-num", new Object[]{String.valueOf(liveDetailDto.getConcurrentUserCount())}, locale))
                    .inline(true)
                    .build();

            fields.add(viewerCountField);
        }

        final DiscordEmbed.Author author = createAuthor(channelData, "stream.online.event-message", locale, ChzzkSubscriptionType.STREAM_ONLINE);

        DiscordEmbed.Image image = null;
        // 기존에 중첩된 조건문을 간소화하여 가독성을 높임
        if (form.isShowThumbnail() && !liveDetailDto.isAdult() && liveDetailDto.getLiveImageUrl() != null) {
            image = DiscordEmbed.Image.builder()
                .url(liveDetailDto.getLiveImageUrl().replace("{type}", "480"))
                .build();
        } else {
            if (form.isShowThumbnail()) {
                if (liveDetailDto.isAdult()) {
                    log.info("Stream was set as adult stream. Thumbnail is not exists. Channel ID: {}", channelData.getChannelId());
                } else {
                    log.info("Live Image URL is NULL. Channel ID: {}", channelData.getChannelId());
                }
            }
        }

        final List<DiscordEmbed> discordEmbeds = new ArrayList<>();
        final DiscordEmbed discordEmbed = createDiscordEmbed(author, embedTitle, liveURL, embedDescription, embedColor, fields, footer, String.valueOf(startTime), thumbnail, image);
        discordEmbeds.add(discordEmbed);

        return new DiscordEmbed.Webhook(form.getBotProfileId().getUsername(),
                form.getBotProfileId().getAvatarUrl(), form.getContent(), discordEmbeds);
    }

    private DiscordEmbed.Webhook makeStreamOfflineDiscordWebhook(final ChzzkSubscriptionFormEntity form, final ChzzkChannelDto channelData) {
        // Form의 Locale 얻기
        final Locale locale = Locale.forLanguageTag(form.getLanguageIsoData().getCode());

        // Author Area
        // https://chzzk.naver.com/<Channel ID>
        final String liveURL = String.format("%s/%s", chzzkUrl, channelData.getChannelId());

        // Embed Area
        final String embedColor = Integer.toString(form.getDecimalColor());
        final String embedDescription = messageSource.getMessage("stream.offline.embed-description", null, locale);
        final String embedTitle = messageSource.getMessage("stream.offline.embed-title", null, locale);

        // Embed Field Area
        final List<DiscordEmbed.Field> fields = new ArrayList<>();

        // Embed Footer Area
        final DiscordEmbed.Footer footer = new DiscordEmbed.Footer(messageSource.getMessage("stream.offline.footer", null, locale), chzzkFaviconUrl);

        // Embed Timestamp -> KST로 오기 때문에 UTC로 변환.
        final LocalDateTime submitTime = ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime();

        final DiscordEmbed.Author author = createAuthor(channelData, "stream.offline.event-message", locale, ChzzkSubscriptionType.STREAM_OFFLINE);

        final List<DiscordEmbed> discordEmbeds = new ArrayList<>();
        final DiscordEmbed discordEmbed = createDiscordEmbed(author, embedTitle, liveURL, embedDescription, embedColor, fields, footer, String.valueOf(submitTime), null, null);
        discordEmbeds.add(discordEmbed);

        return new DiscordEmbed.Webhook(form.getBotProfileId().getUsername(),
                form.getBotProfileId().getAvatarUrl(), form.getContent(), discordEmbeds);
    }

    private DiscordEmbed.Webhook makeChannelUpdateDiscordWebhook(final ChzzkSubscriptionFormEntity form, final ChzzkChannelDto afterChannelData) {
        // Form의 Locale 얻기
        final Locale locale = Locale.forLanguageTag(form.getLanguageIsoData().getCode());

        // Author Area
        // https://chzzk.naver.com/<Channel ID>
        final String liveURL = String.format("%s/%s", chzzkUrl, afterChannelData.getChannelId());

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
        final DiscordEmbed.Footer footer = new DiscordEmbed.Footer(messageSource.getMessage("channel.update.footer", null, locale), chzzkFaviconUrl);

        // Embed Timestamp -> KST로 오기 때문에 UTC로 변환.
        final LocalDateTime submitTime = ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime();

        final DiscordEmbed.Author author = createAuthor(afterChannelData, "channel.update.event-message", locale, ChzzkSubscriptionType.CHANNEL_UPDATE);

        final List<DiscordEmbed> discordEmbeds = new ArrayList<>();
        final DiscordEmbed discordEmbed = createDiscordEmbed(author, embedTitle, liveURL, embedDescription, embedColor, fields, footer, String.valueOf(submitTime), null, null);
        discordEmbeds.add(discordEmbed);

        return new DiscordEmbed.Webhook(form.getBotProfileId().getUsername(),
                form.getBotProfileId().getAvatarUrl(), form.getContent(), discordEmbeds);
    }

    private DiscordEmbed.Author createAuthor(final ChzzkChannelDto channelData, final String messageKey,
                                             final Locale locale, final ChzzkSubscriptionType subscriptionType) {
        String liveURL = String.format("%s/%s", chzzkUrl, channelData.getChannelId());

        if (subscriptionType == ChzzkSubscriptionType.STREAM_ONLINE) {
            liveURL = String.format("%s/live/%s", chzzkUrl, channelData.getChannelId());
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
