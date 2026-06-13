package me.cocoblue.chzzkeventtodiscord.service.soop;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.domain.soop.SoopSubscriptionEntity;
import me.cocoblue.chzzkeventtodiscord.domain.soop.SoopSubscriptionRepository;
import me.cocoblue.chzzkeventtodiscord.dto.soop.SoopLiveStatusDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class SoopEventReader {
    private final SoopSubscriptionRepository soopSubscriptionRepository;
    private final SoopLiveStatusService soopLiveStatusService;
    private final SoopEventSender soopEventSender;

    @Value("${app.is-test:false}")
    private boolean isTest;

    @Scheduled(fixedRateString = "#{${soop.check-interval:30} * 1000}")
    public void readEvent() {
        if (isTest) {
            return;
        }
        soopSubscriptionRepository.findAllByEnabled(true).forEach(this::classifyAndSend);
    }

    @Transactional
    public void classifyAndSend(SoopSubscriptionEntity subscription) {
        final boolean wasLive = subscription.isLive();
        final SoopLiveStatusDto status = soopLiveStatusService.getLiveStatus(subscription.getSoopUserId());
        if (!wasLive && status.live() && subscription.isNotifyOnline()) {
            soopEventSender.sendOnlineEvent(subscription, status);
        } else if (wasLive && !status.live() && subscription.isNotifyOffline()) {
            soopEventSender.sendOfflineEvent(subscription, status);
        }

        subscription.setSoopChannelName(status.userNick());
        subscription.setProfileImageUrl(status.profileImageUrl());
        subscription.setLive(status.live());
        subscription.setLiveTitle(status.title());
        subscription.setBroadNo(status.broadNo());
        subscription.setLiveUrl(status.liveUrl());
    }
}
