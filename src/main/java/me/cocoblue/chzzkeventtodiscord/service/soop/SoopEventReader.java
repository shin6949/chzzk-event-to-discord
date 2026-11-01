package me.cocoblue.chzzkeventtodiscord.service.soop;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.domain.soop.SoopSubscriptionEntity;
import me.cocoblue.chzzkeventtodiscord.domain.soop.SoopSubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class SoopEventReader {
    private final SoopSubscriptionRepository soopSubscriptionRepository;
    private final SoopSubscriptionEventProcessor soopSubscriptionEventProcessor;

    @Value("${app.is-test:false}")
    private boolean isTest;

    @Scheduled(fixedRateString = "#{${soop.check-interval:30} * 1000}")
    public void readEvent() {
        if (isTest) {
            return;
        }
        soopSubscriptionRepository.findAllByEnabled(true).stream()
            .map(SoopSubscriptionEntity::getId)
            .forEach(soopSubscriptionEventProcessor::classifyAndSend);
    }
}
