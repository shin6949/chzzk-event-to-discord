package me.cocoblue.chzzkeventtodiscord.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import dev.akkinoc.util.YamlResourceBundle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    @Bean
    public CookieLocaleResolver localeResolver() {
        final CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        localeResolver.setCookieName("locale");
        localeResolver.setCookieMaxAge(60 * 60);
        localeResolver.setCookiePath("/");

        return localeResolver;
    }

    @Bean
    public MessageSource messageSource(@Value("${message.basenames}") List<String> basenames) {
        final YamlMessageSource messageSource = new YamlMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultLocale(Locale.ENGLISH);

        String[] messageList = new String[basenames.size()];
        messageList = basenames.toArray(messageList);
        messageSource.setBasenames(messageList);
        return messageSource;
    }
}

class YamlMessageSource extends ResourceBundleMessageSource {
    @Override
    protected ResourceBundle doGetBundle(String basename, Locale locale) throws MissingResourceException {
        return ResourceBundle.getBundle(basename, locale, YamlResourceBundle.Control.INSTANCE);
    }
}
