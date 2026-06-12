package me.cocoblue.chzzkeventtodiscord.dto.chzzk;

import lombok.*;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;

/**
 * {@code ChzzkChannelDto}는 API 요청/응답 데이터 전송 구조를 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChzzkChannelDto {
  private String channelId;
  private String channelName;
  private String channelImageUrl;
  private Boolean verifiedMark;
  private String channelDescription;
  private int followerCount;
  private boolean openLive;
  private boolean subscriptionAvailability;

  /**
   * {@code ChzzkChannelDto} 생성자는 인스턴스 초기 상태를 구성합니다.
   *
   * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
   *
   * @since Ver.0.1.3
   */
  public ChzzkChannelDto(ChzzkChannelEntity entity) {
    this.channelId = entity.getChannelId();
    this.channelName = entity.getChannelName();
    this.channelImageUrl = entity.getProfileUrl();
    this.verifiedMark = entity.isVerifiedMark();
    this.channelDescription = entity.getChannelDescription();
    this.followerCount = entity.getFollowerCount();
    this.subscriptionAvailability = entity.isSubscriptionAvailability();
    this.openLive = entity.isLive();
  }

  /**
   * {@code toEntity}은 다른 데이터 형식으로 변환합니다.
   *
   * <p>Git 이력: 생성 2024-03-04 02:28:28 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 HEAD blame 커밋 db09ddc.
   *
   * @since Ver.0.1
   */
  public ChzzkChannelEntity toEntity() {
    return ChzzkChannelEntity.builder()
        .channelId(channelId)
        .channelName(channelName)
        .isVerifiedMark(Boolean.TRUE.equals(verifiedMark))
        .profileUrl(channelImageUrl)
        .channelDescription(channelDescription)
        .followerCount(followerCount)
        .subscriptionAvailability(subscriptionAvailability)
        .isLive(openLive)
        .build();
  }
}
