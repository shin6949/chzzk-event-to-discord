package me.cocoblue.chzzkeventtodiscord.service;

import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.ChzzkEventToDiscordApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class AppStartupRunner implements CommandLineRunner {
    @Value("${chzzk.api-url:null}")
    private String CHZZK_API_URL;

    @Override
    public void run(String[] args) {
        // 신규 API URL이 설정되어 있다면, 기본 URL을 변경한다.
        if (!CHZZK_API_URL.equals("null") && !CHZZK_API_URL.isEmpty() &&
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
