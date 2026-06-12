package me.cocoblue.chzzkeventtodiscord.vo.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableConditionType;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableGroupType;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkLiveStatusType;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkLiveStatusDto;

/**
 * {@code ChzzkLiveStatusApiResponseVo}는 외부 API 응답 값을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChzzkLiveStatusApiResponseVo extends ChzzkApiCommonResponseVo {
  @JsonProperty("content")
  private ChzzkLiveStatusVo content;

  /**
   * {@code toDto}은 다른 데이터 형식으로 변환합니다.
   *
   * <p>Git 이력: 생성 2024-03-26 21:35:21 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 HEAD blame 커밋
   * 21ff52a.
   *
   * @since Ver.0.1.3
   */
  public ChzzkLiveStatusDto toDto() {
    return ChzzkLiveStatusDto.builder()
        .liveTitle(content.getLiveTitle())
        .status(content.getStatus())
        .concurrentUserCount(content.getConcurrentUserCount())
        .accumulateCount(content.getAccumulateCount())
        .paidPromotion(content.isPaidPromotion())
        .adult(content.isAdult())
        .chatChannelId(content.getChatChannelId())
        .chatActive(content.isChatActive())
        .chatAvailableGroup(content.getChatAvailableGroup())
        .chatAvailableCondition(content.getChatAvailableCondition())
        .minFollowerMinute(content.getMinFollowerMinute())
        .categoryType(content.getCategoryType())
        .categoryId(content.getCategoryId())
        .categoryValue(content.getCategoryValue())
        .chatDonationRankingExposure(content.isChatDonationRankingExposure())
        .build();
  }
}

/**
 * {@code ChzzkLiveStatusVo}는 외부 API 응답 값을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class ChzzkLiveStatusVo {
  @JsonProperty("liveTitle")
  private String liveTitle;

  @JsonProperty("status")
  private ChzzkLiveStatusType status;

  @JsonProperty("concurrentUserCount")
  private Long concurrentUserCount;

  @JsonProperty("accumulateCount")
  private Long accumulateCount;

  @JsonProperty("paidPromotion")
  private boolean paidPromotion;

  @JsonProperty("adult")
  private boolean adult;

  @JsonProperty("chatChannelId")
  private String chatChannelId;

  @JsonProperty("livePollingStatusJson")
  private String livePollingStatusJson;

  @JsonProperty("faultStatus")
  private ChzzkLiveStatusType faultStatus;

  @JsonProperty("userAdultStatus")
  private String userAdultStatus;

  @JsonProperty("chatActive")
  private boolean chatActive;

  @JsonProperty("chatAvailableGroup")
  private ChzzkChatAvailableGroupType chatAvailableGroup;

  @JsonProperty("chatAvailableCondition")
  private ChzzkChatAvailableConditionType chatAvailableCondition;

  @JsonProperty("minFollowerMinute")
  private Long minFollowerMinute;

  @JsonProperty("categoryType")
  private String categoryType;

  @JsonProperty("liveCategory")
  private String categoryId;

  @JsonProperty("liveCategoryValue")
  private String categoryValue;

  /* @since Chzzk 24.03.21. Update */
  @JsonProperty("chatDonationRankingExposure")
  private boolean chatDonationRankingExposure;
}
