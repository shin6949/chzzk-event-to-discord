package me.cocoblue.chzzkeventtodiscord.vo.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkChannelVo;

/**
 * {@code ChzzkChannelInfoApiResponseVo}는 외부 API 응답 값을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
 *
 * @since Ver.0.1.3
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChzzkChannelInfoApiResponseVo extends ChzzkApiCommonResponseVo {
  @JsonProperty("content")
  private ChzzkChannelInfoContentVo content;

  /**
   * {@code getFirstChannel}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거
   * HEAD blame 커밋 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public ChzzkChannelVo getFirstChannel() {
    if (content == null || content.data == null || content.data.isEmpty()) {
      return null;
    }
    return content.data.get(0);
  }

  /**
   * {@code ChzzkChannelInfoContentVo}는 외부 API 응답 값을 표현합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거
   * HEAD blame 커밋 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ChzzkChannelInfoContentVo {
    @JsonProperty("data")
    private List<ChzzkChannelVo> data;
  }
}
