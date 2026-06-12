package me.cocoblue.chzzkeventtodiscord.data.chzzk;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@code ChzzkSubscriptionType}는 애플리케이션에서 사용하는 코드 값을 정의합니다.
 *
 * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 HEAD blame 커밋 5150c0f.
 *
 * @since Ver.0.1
 */
@Getter
@AllArgsConstructor
public enum ChzzkSubscriptionType {
  STREAM_ONLINE,
  STREAM_OFFLINE,
  CHANNEL_UPDATE,
  /* @since 1.2 */
  STREAM_ONLINE_AND_OFFLINE,
}
