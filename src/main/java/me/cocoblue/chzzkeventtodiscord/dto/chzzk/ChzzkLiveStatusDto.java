package me.cocoblue.chzzkeventtodiscord.dto.chzzk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableConditionType;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableGroupType;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkLiveStatusType;

/**
 * {@code ChzzkLiveStatusDto}는 API 요청/응답 데이터 전송 구조를 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkLiveStatusDto {
  private String liveTitle;
  private ChzzkLiveStatusType status;
  private Long concurrentUserCount;
  private Long accumulateCount;
  private boolean paidPromotion;
  private boolean adult;
  private String chatChannelId;
  private boolean chatActive;
  private ChzzkChatAvailableGroupType chatAvailableGroup;
  private ChzzkChatAvailableConditionType chatAvailableCondition;
  private Long minFollowerMinute;
  private String categoryType;
  private String categoryId;
  private String categoryValue;
  private boolean chatDonationRankingExposure;
}
