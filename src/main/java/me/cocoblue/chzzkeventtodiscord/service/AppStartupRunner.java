package me.cocoblue.chzzkeventtodiscord.service;

import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.ChzzkEventToDiscordApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class AppStartupRunner implements CommandLineRunner {

    @Value("${chzzk.api-url}")
    private String CHZZK_API_URL;

    @Override
    public void run(String[] args) {
        if(CHZZK_API_URL != null && !CHZZK_API_URL.isEmpty() &&
                !CHZZK_API_URL.equals(ChzzkEventToDiscordApplication.CHZZK_API_URL)) {
            if (CHZZK_API_URL.endsWith("/")) {
                log.info("Removing the trailing slash from the other Chzzk API URL: " + CHZZK_API_URL);
                CHZZK_API_URL = CHZZK_API_URL.substring(0, CHZZK_API_URL.length() - 1);
            }

            log.info("Other Chzzk API URL is presented. Overriding the default URL: " + CHZZK_API_URL);
            ChzzkEventToDiscordApplication.CHZZK_API_URL = CHZZK_API_URL;
        }
    }
}
