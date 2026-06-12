package me.cocoblue.chzzkeventtodiscord.dto.chzzk;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableConditionType;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableGroupType;

/**
 * {@code ChzzkLiveDetailDto}는 API 요청/응답 데이터 전송 구조를 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkLiveDetailDto {
  protected String liveId;
  protected String liveTitle;
  protected String liveImageUrl;
  protected String defaultThumbnailImageUrl;
  protected int concurrentUserCount;
  protected int accumulateCount;
  protected LocalDateTime openDate;
  private LocalDateTime closeDate;
  protected String chatChannelId;
  protected String categoryType;
  protected String categoryId;
  protected String categoryValue;
  private boolean adult;
  private boolean chatActive;
  private ChzzkChatAvailableGroupType chatAvailableGroup;
  private ChzzkChatAvailableConditionType chatAvailableCondition;
  private int minFollowerMinute;
  // After 2024-04-24
  private List<String> tags;
}
