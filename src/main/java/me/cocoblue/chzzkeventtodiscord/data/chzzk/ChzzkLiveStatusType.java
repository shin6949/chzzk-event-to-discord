package me.cocoblue.chzzkeventtodiscord.data.chzzk;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@code ChzzkLiveStatusType}는 애플리케이션에서 사용하는 코드 값을 정의합니다.
 *
 * <p>Git 이력: 생성 2024-02-29 16:00:04 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 1605357.
 *
 * @since Ver.0.1
 */
@Getter
@AllArgsConstructor
public enum ChzzkLiveStatusType {
  CLOSE("CLOSE"),
  OPEN("OPEN"),
  // API 변동에 대비한 기본 값
  UNKNOWN("UNKNOWN");

  private final String value;

  /**
   * {@code forValue}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2024-02-29 16:00:04 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 1605357.
   *
   * @since Ver.0.1
   */
  public static ChzzkLiveStatusType forValue(String value) {
    for (ChzzkLiveStatusType type : values()) {
      if (type.getValue().equals(value)) {
        return type;
      }
    }
    return UNKNOWN; // 알 수 없는 값 처리
  }

  /**
   * {@code getValue}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-02-29 16:00:04 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 1605357.
   *
   * @since Ver.0.1
   */
  @JsonValue // 이 어노테이션을 추가하여 직렬화할 때 사용할 값을 지정합니다.
  public String getValue() {
    return value;
  }
}
