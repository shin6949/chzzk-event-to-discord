package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code ChzzkCategoryId}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2024-02-28 21:28:31 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1, 근거 커밋 99215d6.
 *
 * @since Ver.0.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Builder
public class ChzzkCategoryId implements Serializable {
  @Column(name = "category_id", nullable = false)
  private String categoryId;

  @Column(name = "category_type", nullable = false)
  private String categoryType;
}
