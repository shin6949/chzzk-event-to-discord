package me.cocoblue.chzzkeventtodiscord.domain.eventlog;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import lombok.*;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;
import org.hibernate.annotations.CreationTimestamp;

/**
 * {@code NotificationLogEntity}는 데이터베이스 테이블 매핑을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-03 02:51:29 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 4bc1c09.
 *
 * @since Ver.0.1
 */
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
  @JoinColumn(
      name = "form_id",
      foreignKey = @ForeignKey(name = "fk_notification_log_form_id"),
      nullable = false)
  private ChzzkSubscriptionFormEntity subscriptionForm;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private ZonedDateTime createdAt;
}
