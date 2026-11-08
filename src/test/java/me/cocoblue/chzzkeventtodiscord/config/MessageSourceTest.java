package me.cocoblue.chzzkeventtodiscord.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

// Guards the YAML bundle loading: a dependency (e.g. Kotlin stdlib) mismatch makes every locale fail at runtime only.
class MessageSourceTest {
    private final MessageSource messageSource = new WebConfig().messageSource(List.of("string/string"));

    @Test
    void loadsYamlBundleForEveryLocale() {
        for (final String tag : List.of("ko", "en")) {
            final Locale locale = Locale.forLanguageTag(tag);
            assertEquals(":video_game: ", messageSource.getMessage("game.prefix", null, locale));
            assertNotNull(messageSource.getMessage("channel.update.subscription-available", null, locale));
        }
    }
}
