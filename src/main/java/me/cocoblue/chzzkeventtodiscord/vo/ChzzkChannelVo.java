package me.cocoblue.chzzkeventtodiscord.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkChannelDto;

/**
 * {@code ChzzkChannelVo}는 외부 API 응답 값을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChzzkChannelVo {
  @JsonProperty("channelId")
  private String channelId;

  @JsonProperty("channelName")
  private String channelName;

  @JsonProperty("channelImageUrl")
  private String channelImageUrl;

  @JsonProperty("verifiedMark")
  private boolean verifiedMark;

  @JsonProperty("channelDescription")
  private String channelDescription;

  @JsonProperty("followerCount")
  private int followerCount;

  @JsonProperty("openLive")
  private boolean openLive;

  @JsonProperty("personalData")
  private ChzzkPersonalDataVo personalData;

  @JsonProperty("subscriptionAvailability")
  private boolean subscriptionAvailability;

  @JsonProperty("subscriptionPaymentAvailability")
  private ChzzkSubscriptionPaymentAvailabilityVo subscriptionPaymentAvailability;

  /**
   * {@code toDto}은 다른 데이터 형식으로 변환합니다.
   *
   * <p>Git 이력: 생성 2024-03-26 21:35:21 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 HEAD blame 커밋
   * 21ff52a.
   *
   * @since Ver.0.1.3
   */
  public ChzzkChannelDto toDto() {
    return ChzzkChannelDto.builder()
        .channelId(channelId)
        .channelName(channelName)
        .channelImageUrl(channelImageUrl)
        .verifiedMark(verifiedMark)
        .channelDescription(channelDescription)
        .followerCount(followerCount)
        .openLive(openLive)
        .verifiedMark(verifiedMark)
        .followerCount(followerCount)
        .channelDescription(channelDescription)
        .subscriptionAvailability(subscriptionAvailability)
        .build();
  }
}

/**
 * {@code ChzzkPersonalDataVo}는 외부 API 응답 값을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class ChzzkPersonalDataVo {
  @JsonProperty("following")
  private ChzzkFollowingVo following;

  @JsonProperty("privateUserBlock")
  private boolean privateUserBlock;
}

/**
 * {@code ChzzkFollowingVo}는 외부 API 응답 값을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class ChzzkFollowingVo {
  @JsonProperty("following")
  private boolean following;

  @JsonProperty("notification")
  private boolean notification;

  @JsonProperty("followDate")
  private LocalDate followDate;
}

/**
 * {@code ChzzkSubscriptionPaymentAvailabilityVo}는 외부 API 응답 값을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class ChzzkSubscriptionPaymentAvailabilityVo {
  @JsonProperty("iapAvailability")
  private boolean iapAvailability;

  @JsonProperty("iabAvailability")
  private boolean iabAvailability;
}
