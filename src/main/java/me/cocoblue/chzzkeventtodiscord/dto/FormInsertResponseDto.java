package me.cocoblue.chzzkeventtodiscord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * {@code FormInsertResponseDto}는 API 요청/응답 데이터 전송 구조를 표현합니다.
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
public class FormInsertResponseDto {
  @JsonProperty("isSuccess")
  private Boolean isSuccess;

  @JsonProperty("registeredFormId")
  private Long registeredFormId;

  @JsonProperty("registeredWebhookId")
  private Long registeredWebhookId;

  @JsonProperty("registeredBotProfileId")
  private Long registeredBotProfileId;
}
