package me.cocoblue.chzzkeventtodiscord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import me.cocoblue.chzzkeventtodiscord.data.LanguageIsoData;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkSubscriptionType;

/**
 * {@code FormInsertRequestDto}는 API 요청/응답 데이터 전송 구조를 표현합니다.
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
public class FormInsertRequestDto {
  @JsonProperty("channelId")
  @Size(max = 100)
  private String channelId;

  @JsonProperty("channelName")
  @Size(max = 33)
  private String channelName;

  @JsonProperty("content")
  @Size(max = 2000)
  private String content;

  @JsonProperty("colorHex")
  @Pattern(regexp = "^[0-9A-Fa-f]{6}$")
  private String colorHex;

  @JsonProperty("subscriptionType")
  @NotNull
  private ChzzkSubscriptionType subscriptionType;

  @JsonProperty("showDetail")
  private Boolean showDetail;

  @JsonProperty("webhookId")
  private Long webhookId;

  @JsonProperty("webhookName")
  @Size(max = 100)
  private String webhookName;

  @JsonProperty("webhookUrl")
  @Size(max = 2000)
  private String webhookUrl;

  @JsonProperty("botProfileId")
  private Long botProfileId;

  @JsonProperty("botUsername")
  @Size(max = 100)
  private String botUsername;

  @JsonProperty("botAvatarUrl")
  @Size(max = 2000)
  private String botAvatarUrl;

  @JsonProperty("ownerChannelId")
  @Size(max = 100)
  private String ownerChannelId;

  @JsonProperty("ownerChannelName")
  @Size(max = 33)
  private String ownerChannelName;

  @JsonProperty("intervalMinute")
  @Min(1)
  @Max(1440)
  private Integer intervalMinute;

  @JsonProperty("language")
  private LanguageIsoData language;

  @JsonProperty("enabled")
  private Boolean enabled;
}
