package me.cocoblue.chzzkeventtodiscord.domain.eventlog;

import jakarta.persistence.*;
import lombok.*;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

@Getter
@Setter
@Entity(name = "notification_log")
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id", nullable = false, columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    @ManyToOne()
    @JoinColumn(name="form_id", foreignKey = @ForeignKey(name="FK_NOTIFICATION_LOG_FORM_ID"), nullable = false)
    private ChzzkSubscriptionFormEntity subscriptionForm;

    @CreationTimestamp
    @Column(name="created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;
}
