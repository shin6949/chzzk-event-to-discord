package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import java.io.Serializable;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * {@code ChzzkChannelEntity}는 데이터베이스 테이블 매핑을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
 *
 * @since Ver.0.1
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity(name = "chzzk_channel")
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ChzzkChannelEntity implements Serializable {
  @Id
  @Column(name = "channel_id", nullable = false)
  private String channelId;

  // 30자 제한이지만 여유를 두어 33자로 설정
  @Column(length = 33, name = "channel_name", nullable = false)
  private String channelName;

  // URL 길이 제한 (크롬: 32,779) -> 그러나 Oracle DB의 제한으로 인해 30,000으로 설정
  @Column(length = 30000, name = "profile_url")
  private String profileUrl;

  @Column(name = "is_verified_mark", nullable = false)
  private boolean isVerifiedMark;

  // 500자 제한이지만 여유를 두어 550자로 설정
  @Column(length = 550, name = "channel_description")
  private String channelDescription;

  @Column(name = "subscription_availability", nullable = false)
  @ColumnDefault("0")
  private boolean subscriptionAvailability;

  @Column(name = "is_live", nullable = false)
  @ColumnDefault("0")
  private boolean isLive;

  @Column(name = "follower_count", nullable = false)
  @ColumnDefault("0")
  private int followerCount;

  @Column(name = "last_check_time", nullable = false)
  @UpdateTimestamp
  private ZonedDateTime lastCheckTime;
  //    @Version
  //    @Column(name = "version", nullable = false, columnDefinition = "BIGINT(20) DEFAULT 0")
  //    private Long version;
}
