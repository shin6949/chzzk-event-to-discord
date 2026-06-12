package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

/**
 * {@code ChzzkStreamOnlineFormEntity}는 데이터베이스 테이블 매핑을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Getter
@Setter
@Entity(name = "chzzk_subscription_stream_online_form")
@SuperBuilder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkStreamOnlineFormEntity extends ChzzkSubscriptionFormEntity {
  @Column(name = "show_detail", nullable = false, length = 1)
  @ColumnDefault("0")
  private boolean showDetail;

  @Column(name = "show_thumbnail", nullable = false, length = 1)
  @ColumnDefault("1")
  private boolean showThumbnail;

  @Column(name = "show_viewer_count", nullable = false, length = 1)
  @ColumnDefault("0")
  private boolean showViewerCount;

  @Column(name = "show_tag", nullable = false, length = 1)
  @ColumnDefault("1")
  private boolean showTag;
}
