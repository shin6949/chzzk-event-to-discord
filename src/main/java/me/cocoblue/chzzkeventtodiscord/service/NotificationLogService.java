package me.cocoblue.chzzkeventtodiscord.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;
import me.cocoblue.chzzkeventtodiscord.domain.eventlog.NotificationLogEntity;
import me.cocoblue.chzzkeventtodiscord.domain.eventlog.NotificationLogRepository;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class NotificationLogService {
    private final NotificationLogRepository notificationLogRepository;

    public void insertNotificationLog(final Long subscriptionId) {
        log.info("Insert notification log. subscriptionId: {}", subscriptionId);
        final ChzzkSubscriptionFormEntity subscriptionForm = ChzzkSubscriptionFormEntity.builder()
                .id(subscriptionId)
                .build();

        final NotificationLogEntity notificationLogEntity = NotificationLogEntity.builder()
                .subscriptionForm(subscriptionForm)
                .build();

        notificationLogRepository.save(notificationLogEntity);
    }
}
