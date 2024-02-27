package me.cocoblue.chzzkeventtodiscord.util;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Log4j2
@Component
public class TimeZoneConverter {
    @Value("${app.default-timezone:Asia/Seoul}")
    private String DEFAULT_TIMEZONE;

    public static ZonedDateTime convertToUtc(final ZonedDateTime zonedDateTime) {
        return zonedDateTime.withZoneSameInstant(java.time.ZoneId.of("UTC"));
    }

    public static ZonedDateTime convertToDefaultTimeZone(final ZonedDateTime zonedDateTime) {
        log.info("Converting to default timezone: " + zonedDateTime);
        return zonedDateTime.withZoneSameInstant(java.time.ZoneId.of("Asia/Seoul"));
    }
}
