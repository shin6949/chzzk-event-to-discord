package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * {@code ChzzkCategoryEntity}는 데이터베이스 테이블 매핑을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
 *
 * @since Ver.0.1
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity(name = "chzzk_category")
@Builder
public class ChzzkCategoryEntity {
  @EmbeddedId private ChzzkCategoryId id;

  @Column(name = "category_name", nullable = false)
  private String categoryName;

  @Column(name = "poster_image_url", length = 30000)
  private String posterImageUrl;

  @Column(name = "updated_at", nullable = false)
  @UpdateTimestamp
  private ZonedDateTime updatedAt;
}
