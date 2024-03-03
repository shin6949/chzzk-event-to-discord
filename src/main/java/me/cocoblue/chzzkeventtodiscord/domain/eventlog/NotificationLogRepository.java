package me.cocoblue.chzzkeventtodiscord.domain.eventlog;

import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLogEntity, Long> {
    List<NotificationLogEntity> getCountBySubscriptionFormAndCreatedAtBetween(ChzzkSubscriptionFormEntity subscriptionForm,
                                                                              ZonedDateTime start, ZonedDateTime end);
}
