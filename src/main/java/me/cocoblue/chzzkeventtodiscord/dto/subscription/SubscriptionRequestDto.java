package me.cocoblue.chzzkeventtodiscord.dto.subscription;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import me.cocoblue.chzzkeventtodiscord.data.LanguageIsoData;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkSubscriptionType;

/**
 * {@code SubscriptionRequestDto}는 API 요청/응답 데이터 전송 구조를 표현합니다.
 *
 * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 7fec3f1.
 *
 * @since unreleased after Ver.0.1.4
 */
@Data
public class SubscriptionRequestDto {
  @Size(max = 100)
  private String channelId;

  @Size(max = 100)
  private String formOwnerChannelId;

  private ChzzkSubscriptionType subscriptionType;
  private Long webhookId;
  private Long botProfileId;
  private LanguageIsoData language;

  @Min(1)
  @Max(1440)
  private Integer intervalMinute;

  private Boolean enabled;

  @Size(max = 2000)
  private String content;

  @Pattern(regexp = "^[0-9A-Fa-f]{6}$")
  private String colorHex;

  private Boolean showDetail;
  private Boolean showThumbnail;
  private Boolean showViewerCount;
  private Boolean showTag;
}
