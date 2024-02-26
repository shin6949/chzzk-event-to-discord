package me.cocoblue.chzzkeventtodiscord.service.chzzk;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Log4j2
@Service
@RequiredArgsConstructor
public class ChzzkStreamNotifyService {
    @Value("${twitch.logo-url}")
    private String twitchLogoUrl;

    private final MessageSource messageSource;
    private final String twitchUrl = "https://twitch.tv/";

//    public ChzzkStreamNotifyService(FirebaseInitializer firebaseInitializer, TwitchUserLogService twitchUserLogService,
//                                    EventSubService eventSubService, UserInfoService userInfoService, GameInfoService gameInfoService,
//                                    NotificationFormService notificationFormService, MessageSource messageSource) {
//        super(firebaseInitializer, twitchUserLogService, eventSubService, userInfoService, gameInfoService, notificationFormService);
//        this.messageSource = messageSource;
//    }

    protected DiscordEmbed.Webhook makeDiscordEmbed(final NotificationEvent event, final SubscriptionFormEntity form,
                                                    final Channel channel, final User twitchUser, final Game game) {
        return (form.getTwitchSubscriptionType() == TwitchSubscriptionType.STREAM_ONLINE)
                ? makeStreamOnlineDiscordWebhook(event, form, channel, twitchUser, game)
                : makeStreamOfflineDiscordWebhook(event, form, twitchUser);
    }

    private DiscordEmbed.Webhook makeStreamOnlineDiscordWebhook(final NotificationEvent event, final SubscriptionFormEntity form,
                                                                final Channel channel, final User user, final Game game) {
        // Event 형 변환
        final StreamNotifyRequestEvent streamEvent = (StreamNotifyRequestEvent) event;

        // Form의 Locale 얻기
        final Locale locale = Locale.forLanguageTag(form.getLanguageIsoData().getCode());

        // Author Area
        final String authorURL = twitchUrl + streamEvent.getBroadcasterUserLogin();

        // Thumbnail
        final String thumbnailUrl = game.removeSizeMentionInBoxArtUrl();
        final DiscordEmbed.Thumbnail thumbnail = DiscordEmbed.Thumbnail.builder().url(thumbnailUrl).build();

        // Embed Area
        final String embedColor = Integer.toString(form.getDecimalColor());

        final String gameName = game.getName();
        final String embedDescription = gameName.equals("") ? messageSource.getMessage("game.none", null, locale) : messageSource.getMessage("game.prefix", null, locale) + gameName;
        final String embedTitle = channel.getTitle();

        // Embed Field Area
        final List<DiscordEmbed.Field> fields = new ArrayList<>();

        // Embed Footer Area
        final DiscordEmbed.Footer footer = new DiscordEmbed.Footer(messageSource.getMessage("stream.online.footer", null, locale), twitchLogoUrl);

        // UTC, Embed Timestamp
        final LocalDateTime startTime = streamEvent.getStartedAt();

        final String languageIsoCode = LanguageIsoData.find(channel.getBroadcasterLanguage()).getCode();
        final DiscordEmbed.Field languageField = new DiscordEmbed.Field(messageSource.getMessage("stream.online.language", null, locale),
                messageSource.getMessage("language." + languageIsoCode, null, locale), true);
        fields.add(languageField);

        final DiscordEmbed.Author author = createAuthor(streamEvent, user, "stream.online.event-message", locale);

        final List<DiscordEmbed> discordEmbeds = new ArrayList<>();
        final DiscordEmbed discordEmbed = createDiscordEmbed(author, embedTitle, authorURL, embedDescription, embedColor, fields, footer, String.valueOf(startTime), thumbnail, null);

        discordEmbeds.add(discordEmbed);

        return new DiscordEmbed.Webhook(form.getBotProfileId().getUsername(),
                form.getBotProfileId().getAvatarUrl(), form.getContent(), discordEmbeds);
    }

    private DiscordEmbed.Webhook makeStreamOfflineDiscordWebhook(final NotificationEvent event, final SubscriptionFormEntity form, final  User user) {
        // Event 형 변환
        final StreamNotifyRequestEvent streamEvent = (StreamNotifyRequestEvent) event;

        // Form의 Locale 얻기
        final Locale locale = Locale.forLanguageTag(form.getLanguageIsoData().getCode());
        log.debug("locale: " + locale);

        // Author Area
        final String authorURL = twitchUrl + streamEvent.getBroadcasterUserLogin();

        // Embed Area
        final String embedColor = Integer.toString(form.getDecimalColor());
        final String embedDescription = messageSource.getMessage("stream.offline.embed-description", null, locale);
        final String embedTitle = messageSource.getMessage("stream.offline.embed-title", null, locale);
        final DiscordEmbed.Image image = DiscordEmbed.Image.builder()
                .url(user.getOfflineImageUrl())
                .height(300)
                .width(300)
                .build();

        // Embed Field Area
        final List<DiscordEmbed.Field> fields = new ArrayList<>();

        // Embed Footer Area
        final DiscordEmbed.Footer footer = new DiscordEmbed.Footer(messageSource.getMessage("stream.offline.footer", null, locale), twitchLogoUrl);

        final LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC);

        final DiscordEmbed.Author author = createAuthor(streamEvent, user, "stream.offline.event-message", locale);
        final List<DiscordEmbed> discordEmbeds = new ArrayList<>();
        final DiscordEmbed discordEmbed = createDiscordEmbed(author, embedTitle, authorURL, embedDescription, embedColor, fields, footer, String.valueOf(endTime), null, image);
        discordEmbeds.add(discordEmbed);

        return new DiscordEmbed.Webhook(form.getBotProfileId().getUsername(),
                form.getBotProfileId().getAvatarUrl(), form.getContent(), discordEmbeds);
    }

    private DiscordEmbed.Author createAuthor(final StreamNotifyRequestEvent event, final User user, final String messageKey, final Locale locale) {
        final String authorURL = twitchUrl + event.getBroadcasterUserLogin();
        final String authorProfileURL = user.getProfileImageUrl();
        String authorName;
        if (user.getDisplayName().equals(user.getLogin())) {
            authorName = String.format("%s%s", user.getDisplayName(),
                    messageSource.getMessage(messageKey, null, locale));
        } else {
            authorName = String.format("%s(%s)%s", user.getDisplayName(), user.getLogin(),
                    messageSource.getMessage(messageKey, null, locale));
        }
        return new DiscordEmbed.Author(authorName, authorURL, authorProfileURL);
    }

    private DiscordEmbed createDiscordEmbed(DiscordEmbed.Author author, String embedTitle, String embedUrl, String embedDescription, String embedColor, List<DiscordEmbed.Field> fields, DiscordEmbed.Footer footer, String timestamp, DiscordEmbed.Thumbnail thumbnail, DiscordEmbed.Image image) {
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
