package me.cocoblue.chzzkeventtodiscord.util;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Log4j2
@Component
public class TimeZoneConverter {
    public static ZonedDateTime convertToUtc(final ZonedDateTime zonedDateTime) {
        return zonedDateTime.withZoneSameInstant(java.time.ZoneId.of("UTC"));
    }

    public static ZonedDateTime convertToDefaultTimeZone(final ZonedDateTime zonedDateTime,
                                                         @Value("${app.default-timezone}") String defaultTimezone) {
        log.info("Converting to default timezone from " + zonedDateTime.getZone() + " to: " + zonedDateTime);
        return zonedDateTime.withZoneSameInstant(java.time.ZoneId.of(defaultTimezone));
    }
}
