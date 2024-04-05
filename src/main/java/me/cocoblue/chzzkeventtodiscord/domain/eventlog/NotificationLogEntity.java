package me.cocoblue.chzzkeventtodiscord.domain.eventlog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;
import org.hibernate.annotations.CreationTimestamp;

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
    @Column(name = "log_id", nullable = false)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "form_id", foreignKey = @ForeignKey(name = "fk_notification_log_form_id"), nullable = false)
    private ChzzkSubscriptionFormEntity subscriptionForm;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
}
