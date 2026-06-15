package me.cocoblue.chzzkeventtodiscord.service.soop;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.domain.soop.SoopSubscriptionEntity;
import me.cocoblue.chzzkeventtodiscord.domain.soop.SoopSubscriptionRepository;
import me.cocoblue.chzzkeventtodiscord.dto.soop.SoopLiveStatusDto;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SoopSubscriptionEventProcessor {
    private final SoopSubscriptionRepository soopSubscriptionRepository;
    private final SoopLiveStatusService soopLiveStatusService;
    private final SoopEventSender soopEventSender;

    @Transactional
    public void classifyAndSend(Long subscriptionId) {
        final SoopSubscriptionEntity subscription = soopSubscriptionRepository.findById(subscriptionId)
            .orElse(null);
        if (subscription == null || !subscription.isEnabled()) {
            return;
        }

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
