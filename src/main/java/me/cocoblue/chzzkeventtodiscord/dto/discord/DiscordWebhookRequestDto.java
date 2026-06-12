package me.cocoblue.chzzkeventtodiscord.dto.discord;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * {@code DiscordWebhookRequestDto}는 API 요청/응답 데이터 전송 구조를 표현합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
@Data
public class DiscordWebhookRequestDto {
  @Size(max = 100)
  private String alias;

  @Size(max = 2000)
  private String url;

  @Size(max = 100)
  private String ownerChannelId;
}
