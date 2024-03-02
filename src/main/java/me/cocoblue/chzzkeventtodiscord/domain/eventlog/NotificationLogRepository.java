package me.cocoblue.chzzkeventtodiscord.domain.eventlog;

import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLogEntity, Long> {
    int getCountBySubscriptionFormAndCreatedAtBetween(ChzzkSubscriptionFormEntity subscriptionForm,
                                                                             ZonedDateTime start, ZonedDateTime end);
}
