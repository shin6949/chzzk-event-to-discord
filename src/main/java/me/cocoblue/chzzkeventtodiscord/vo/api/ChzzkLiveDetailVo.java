package me.cocoblue.chzzkeventtodiscord.vo.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.*;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableConditionType;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableGroupType;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkLiveDetailDto;

// For Chzzk Live Detail API
/**
 * {@code ChzzkLiveDetailVo}는 외부 API 응답 값을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Getter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChzzkLiveDetailVo extends ChzzkApiCommonResponseVo {
  @JsonProperty("content")
  private ChzzkLiveDetailVoContent content;

  /**
   * {@code toDto}은 다른 데이터 형식으로 변환합니다.
   *
   * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
   *
   * @since Ver.0.1.3
   */
  public ChzzkLiveDetailDto toDto() {
    return ChzzkLiveDetailDto.builder()
        .liveId(content.getLiveId())
        .liveTitle(content.getLiveTitle())
        .liveImageUrl(content.getLiveImageUrl())
        .defaultThumbnailImageUrl(content.getDefaultThumbnailImageUrl())
        .concurrentUserCount(content.getConcurrentUserCount())
        .accumulateCount(content.getAccumulateCount())
        .openDate(content.getOpenDate())
        .closeDate(content.getCloseDate())
        .chatChannelId(content.getChatChannelId())
        .categoryType(content.getCategoryType())
        .categoryId(content.getLiveCategory())
        .categoryValue(content.getLiveCategoryValue())
        .adult(content.isAdult())
        .chatActive(content.isChatActive())
        .chatAvailableGroup(content.getChatAvailableGroup())
        .chatAvailableCondition(content.getChatAvailableCondition())
        .minFollowerMinute(content.getMinFollowerMinute())
        .tags(content.getTags())
        .build();
  }
}

/**
 * {@code ChzzkLiveDetailVoContent}는 외부 API 응답 값을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-04-03 18:28:42 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.3, 근거 커밋 0da0efa.
 *
 * @since Ver.0.1.3
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
class ChzzkLiveDetailVoContent extends BaseChzzkLiveVo {
  @JsonProperty("closeDate")
  private LocalDateTime closeDate;

  @JsonProperty("adult")
  private boolean adult;

  @JsonProperty("chatActive")
  private boolean chatActive;

  @JsonProperty("chatAvailableGroup")
  private ChzzkChatAvailableGroupType chatAvailableGroup;

  @JsonProperty("chatAvailableCondition")
  private ChzzkChatAvailableConditionType chatAvailableCondition;

  @JsonProperty("minFollowerMinute")
  private int minFollowerMinute;
}
