package me.cocoblue.chzzkeventtodiscord.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkLiveDto;
import me.cocoblue.chzzkeventtodiscord.vo.api.BaseChzzkLiveVo;

// For Chzzk Search API
/**
 * {@code ChzzkLiveVo}는 외부 API 응답 값을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChzzkLiveVo extends BaseChzzkLiveVo {
  @JsonProperty("channelId")
  private String channelId;

  /**
   * {@code toDto}은 다른 데이터 형식으로 변환합니다.
   *
   * <p>Git 이력: 생성 2024-03-26 21:35:21 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 21ff52a.
   *
   * @since Ver.0.1.3
   */
  public ChzzkLiveDto toDto() {
    return ChzzkLiveDto.builder()
        .liveTitle(liveTitle)
        .liveImageUrl(liveImageUrl)
        .defaultThumbnailImageUrl(defaultThumbnailImageUrl)
        .concurrentUserCount(concurrentUserCount)
        .accumulateCount(accumulateCount)
        .openDate(openDate)
        .liveId(liveId)
        .chatChannelId(chatChannelId)
        .categoryType(categoryType)
        .liveCategory(liveCategory)
        .liveCategoryValue(liveCategoryValue)
        .channelId(channelId)
        .build();
  }
}
