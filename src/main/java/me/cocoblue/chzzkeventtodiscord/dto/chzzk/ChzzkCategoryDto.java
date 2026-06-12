package me.cocoblue.chzzkeventtodiscord.dto.chzzk;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkCategoryEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkCategoryId;

/**
 * {@code ChzzkCategoryDto}는 API 요청/응답 데이터 전송 구조를 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkCategoryDto {
  private String categoryType;
  private String categoryId;
  private String categoryValue;
  private String posterImageUrl;

  /**
   * {@code ChzzkCategoryDto} 생성자는 인스턴스 초기 상태를 구성합니다.
   *
   * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
   *
   * @since Ver.0.1.3
   */
  public ChzzkCategoryDto(ChzzkCategoryEntity entity) {
    this.categoryType = entity.getId().getCategoryType();
    this.categoryId = entity.getId().getCategoryId();
    this.categoryValue = entity.getCategoryName();
    this.posterImageUrl = entity.getPosterImageUrl();
  }

  /**
   * {@code toEntity}은 다른 데이터 형식으로 변환합니다.
   *
   * <p>Git 이력: 생성 2024-03-04 14:56:25 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 HEAD blame 커밋 517f42a.
   *
   * @since Ver.0.1
   */
  public ChzzkCategoryEntity toEntity() {
    return ChzzkCategoryEntity.builder()
        .id(ChzzkCategoryId.builder().categoryId(categoryId).categoryType(categoryType).build())
        .categoryName(categoryValue)
        .posterImageUrl(posterImageUrl)
        .updatedAt(ZonedDateTime.now())
        .build();
  }
}
