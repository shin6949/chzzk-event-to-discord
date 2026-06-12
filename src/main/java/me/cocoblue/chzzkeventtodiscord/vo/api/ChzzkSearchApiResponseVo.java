package me.cocoblue.chzzkeventtodiscord.vo.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkChannelVo;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkLiveVo;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkVideoVo;

/**
 * {@code ChzzkSearchApiResponseVo}는 외부 API 응답 값을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChzzkSearchApiResponseVo extends ChzzkApiCommonResponseVo {
  @JsonProperty("content")
  private ChzzkContentVo content;

  /**
   * {@code getContentSize}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-03-01 00:49:18 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 HEAD blame 커밋 ad3cd2f.
   *
   * @since Ver.0.1
   */
  public int getContentSize() {
    return content.getSize();
  }

  /**
   * {@code getChannel}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 HEAD blame 커밋
   * 6a6084e.
   *
   * @since Ver.0.1.3
   */
  public ChzzkChannelVo getChannel(int index) {
    return content.getData().get(index).getChannel();
  }

  /**
   * {@code getLive}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 HEAD blame 커밋
   * 6a6084e.
   *
   * @since Ver.0.1.3
   */
  public ChzzkLiveVo getLive(int index) {
    if (content.getData().get(index).getContent() == null) {
      return null;
    }
    return content.getData().get(index).getContent().getLive();
  }
}

/**
 * {@code ChzzkContentVo}는 외부 API 응답 값을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class ChzzkContentVo {
  @JsonProperty("size")
  private int size;

  @JsonProperty("page")
  private Map<String, Object> page;

  @JsonProperty("data")
  private List<ChzzkApiResponseData> data;
}

/**
 * {@code ChzzkApiResponseData}는 외부 API 응답 값을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class ChzzkApiResponseData {
  @JsonProperty("channel")
  private ChzzkChannelVo channel;

  @JsonProperty("content")
  private ChzzkContentDetailVo content;
}

/**
 * {@code ChzzkContentDetailVo}는 외부 API 응답 값을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class ChzzkContentDetailVo {
  @JsonProperty("live")
  private ChzzkLiveVo live;

  @JsonProperty("videos")
  private List<ChzzkVideoVo> videos;
}
