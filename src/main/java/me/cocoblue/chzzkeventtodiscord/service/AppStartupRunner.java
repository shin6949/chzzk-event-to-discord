package me.cocoblue.chzzkeventtodiscord.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.ChzzkEventToDiscordApplication;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;
import me.cocoblue.chzzkeventtodiscord.service.chzzk.ChzzkChannelService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class AppStartupRunner implements CommandLineRunner {
    @Value("${chzzk.api-url:null}")
    private String CHZZK_API_URL;
    @Value("${app.is-test:false}")
    private boolean isTest;
    private final ChzzkSubscriptionFormService subscriptionFormService;
    private final ChzzkChannelService chzzkChannelService;

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

        // 첫 실행 때, 활성화된 Subscription의 Channel Database 갱신
        if(isTest) {
            log.info("Test mode is enabled. Skip Renew Channel Database.");
        } else {
            final List<ChzzkSubscriptionFormEntity> subscriptionFormsAllEnabled = subscriptionFormService.findAllByEnabled(true);
            log.info("Renew Channel Database at the first run.");
            log.info("Need to renew channel ids count: {}", subscriptionFormsAllEnabled.size());
            final Set<String> needToFetchChannelIds = subscriptionFormsAllEnabled.parallelStream()
                .map(ChzzkSubscriptionFormEntity::getChzzkChannelEntity)
                .map(ChzzkChannelEntity::getChannelId)
                .collect(Collectors.toSet());

            needToFetchChannelIds.forEach(chzzkChannelService::getChannelByChannelIdAtAPI);
            log.info("Renew Channel Database finished.");
        }
    }
}
