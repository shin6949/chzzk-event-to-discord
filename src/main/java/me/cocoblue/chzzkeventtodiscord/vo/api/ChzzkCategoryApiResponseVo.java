package me.cocoblue.chzzkeventtodiscord.vo.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import lombok.*;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkCategoryDto;

/**
 * {@code ChzzkCategoryApiResponseVo}는 외부 API 응답 값을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Data
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChzzkCategoryApiResponseVo {
  @JsonProperty("code")
  private int code;

  @JsonProperty("message")
  private String message;

  @JsonProperty("content")
  private ChzzkCategoryContent content;

  /**
   * {@code toDto}은 다른 데이터 형식으로 변환합니다.
   *
   * <p>Git 이력: 생성 2024-03-26 21:35:21 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 HEAD blame 커밋
   * 21ff52a.
   *
   * @since Ver.0.1.3
   */
  public ChzzkCategoryDto toDto() {
    return ChzzkCategoryDto.builder()
        .categoryType(content.getCategoryType())
        .categoryId(content.getCategoryId())
        .categoryValue(content.getCategoryValue())
        .posterImageUrl(content.getPosterImageUrl())
        .build();
  }
}

/**
 * {@code ChzzkCategoryContent}는 외부 API 응답 값을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-04 02:28:28 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 HEAD blame 커밋 db09ddc.
 *
 * @since Ver.0.1
 */
@Data
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ChzzkCategoryContent {
  @JsonProperty("categoryType")
  private String categoryType;

  @JsonProperty("categoryId")
  private String categoryId;

  @JsonProperty("categoryValue")
  private String categoryValue;

  @JsonProperty("posterImageUrl")
  private String posterImageUrl;

  @JsonProperty("openLiveCount")
  private Long openLiveCount;

  @JsonProperty("concurrentUserCount")
  private Long concurrentUserCount;

  @JsonProperty("tags")
  private Set<String> tags;

  @JsonProperty("existLounge")
  private boolean existLounge;
}
