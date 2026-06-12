package me.cocoblue.chzzkeventtodiscord.dto.chzzk;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code ChzzkLiveDto}는 API 요청/응답 데이터 전송 구조를 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkLiveDto {
  private String liveTitle;
  private String liveImageUrl;
  private String defaultThumbnailImageUrl;
  private int concurrentUserCount;
  private int accumulateCount;
  private LocalDateTime openDate;
  private String liveId;
  private String chatChannelId;
  private String categoryType;
  private String liveCategory;
  private String liveCategoryValue;
  private String channelId;
}
