package me.cocoblue.chzzkeventtodiscord.vo.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code BaseChzzkLiveVo}는 외부 API 응답 값을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseChzzkLiveVo {
  @JsonProperty("liveId")
  protected String liveId;

  @JsonProperty("liveTitle")
  protected String liveTitle;

  @JsonProperty("liveImageUrl")
  protected String liveImageUrl;

  @JsonProperty("defaultThumbnailImageUrl")
  protected String defaultThumbnailImageUrl;

  @JsonProperty("concurrentUserCount")
  protected int concurrentUserCount;

  @JsonProperty("accumulateCount")
  protected int accumulateCount;

  @JsonProperty("openDate")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  protected LocalDateTime openDate;

  @JsonProperty("chatChannelId")
  protected String chatChannelId;

  @JsonProperty("categoryType")
  protected String categoryType;

  @JsonProperty("liveCategory")
  protected String liveCategory;

  @JsonProperty("liveCategoryValue")
  protected String liveCategoryValue;

  @JsonProperty("livePlaybackJson")
  protected String livePlaybackJson;

  // After 2024-04-24
  @JsonProperty("tags")
  protected List<String> tags;
}
