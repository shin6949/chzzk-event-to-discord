package me.cocoblue.chzzkeventtodiscord.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.dto.PageResponseDto;
import me.cocoblue.chzzkeventtodiscord.dto.discord.DiscordBotProfileResponseDto;
import me.cocoblue.chzzkeventtodiscord.dto.discord.DiscordWebhookRequestDto;
import me.cocoblue.chzzkeventtodiscord.dto.discord.DiscordWebhookResponseDto;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import me.cocoblue.chzzkeventtodiscord.security.SecretEncryptionService;
import me.cocoblue.chzzkeventtodiscord.service.DiscordBotProfileResourceService;
import me.cocoblue.chzzkeventtodiscord.service.DiscordWebhookResourceService;
import me.cocoblue.chzzkeventtodiscord.service.StaticContentUrlResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * {@code DiscordResourceController}는 HTTP API 요청을 받아 서비스 계층으로 위임합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
@Validated
@RestController
@RequestMapping("/api/v1/discord")
@RequiredArgsConstructor
public class DiscordResourceController {
  private final DiscordWebhookResourceService discordWebhookResourceService;
  private final DiscordBotProfileResourceService discordBotProfileResourceService;
  private final StaticContentUrlResolver staticContentUrlResolver;
  private final SecretEncryptionService secretEncryptionService;

  /**
   * {@code listWebhooks}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @GetMapping("/webhooks")
  public ResponseEntity<PageResponseDto<DiscordWebhookResponseDto>> listWebhooks(
      Pageable pageable, Authentication authentication) {
    final Page<DiscordWebhookResponseDto> response =
        discordWebhookResourceService
            .list(extractPrincipal(authentication), pageable)
            .map(entity -> DiscordWebhookResponseDto.fromEntity(entity, secretEncryptionService));
    return ResponseEntity.ok(PageResponseDto.from(response));
  }

  /**
   * {@code getWebhook}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @GetMapping("/webhooks/{webhookId}")
  public ResponseEntity<DiscordWebhookResponseDto> getWebhook(
      @PathVariable Long webhookId, Authentication authentication) {
    return ResponseEntity.ok(
        DiscordWebhookResponseDto.fromEntity(
            discordWebhookResourceService.get(webhookId, extractPrincipal(authentication)),
            secretEncryptionService));
  }

  /**
   * {@code createWebhook}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @PostMapping("/webhooks")
  public ResponseEntity<DiscordWebhookResponseDto> createWebhook(
      @Valid @RequestBody DiscordWebhookRequestDto request, Authentication authentication) {
    final DiscordWebhookResponseDto response =
        DiscordWebhookResponseDto.fromEntity(
            discordWebhookResourceService.create(request, extractPrincipal(authentication)),
            secretEncryptionService);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * {@code updateWebhook}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @PutMapping("/webhooks/{webhookId}")
  public ResponseEntity<DiscordWebhookResponseDto> updateWebhook(
      @PathVariable Long webhookId,
      @Valid @RequestBody DiscordWebhookRequestDto request,
      Authentication authentication) {
    return ResponseEntity.ok(
        DiscordWebhookResponseDto.fromEntity(
            discordWebhookResourceService.update(
                webhookId, request, extractPrincipal(authentication)),
            secretEncryptionService));
  }

  /**
   * {@code deleteWebhook}은 데이터를 삭제하거나 무효화합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @DeleteMapping("/webhooks/{webhookId}")
  public ResponseEntity<Void> deleteWebhook(
      @PathVariable Long webhookId, Authentication authentication) {
    discordWebhookResourceService.delete(webhookId, extractPrincipal(authentication));
    return ResponseEntity.noContent().build();
  }

  /**
   * {@code listBotProfiles}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @GetMapping("/bot-profiles")
  public ResponseEntity<PageResponseDto<DiscordBotProfileResponseDto>> listBotProfiles(
      Pageable pageable, Authentication authentication) {
    final Page<DiscordBotProfileResponseDto> response =
        discordBotProfileResourceService
            .list(extractPrincipal(authentication), pageable)
            .map(
                entity ->
                    DiscordBotProfileResponseDto.fromEntity(entity, staticContentUrlResolver));
    return ResponseEntity.ok(PageResponseDto.from(response));
  }

  /**
   * {@code getBotProfile}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @GetMapping("/bot-profiles/{botProfileId}")
  public ResponseEntity<DiscordBotProfileResponseDto> getBotProfile(
      @PathVariable Long botProfileId, Authentication authentication) {
    return ResponseEntity.ok(
        DiscordBotProfileResponseDto.fromEntity(
            discordBotProfileResourceService.get(botProfileId, extractPrincipal(authentication)),
            staticContentUrlResolver));
  }

  /**
   * {@code createBotProfile}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @PostMapping(value = "/bot-profiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<DiscordBotProfileResponseDto> createBotProfile(
      @NotBlank @Size(max = 100) @RequestParam String alias,
      @NotBlank @Size(max = 100) @RequestParam String username,
      @RequestParam(required = false) String ownerChannelId,
      @RequestPart MultipartFile avatar,
      Authentication authentication) {
    final DiscordBotProfileResponseDto response =
        DiscordBotProfileResponseDto.fromEntity(
            discordBotProfileResourceService.create(
                alias, username, avatar, ownerChannelId, extractPrincipal(authentication)),
            staticContentUrlResolver);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * {@code updateBotProfile}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @PutMapping(
      value = "/bot-profiles/{botProfileId}",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<DiscordBotProfileResponseDto> updateBotProfile(
      @PathVariable Long botProfileId,
      @Size(max = 100) @RequestParam(required = false) String alias,
      @Size(max = 100) @RequestParam(required = false) String username,
      @RequestPart(required = false) MultipartFile avatar,
      Authentication authentication) {
    return ResponseEntity.ok(
        DiscordBotProfileResponseDto.fromEntity(
            discordBotProfileResourceService.update(
                botProfileId, alias, username, avatar, extractPrincipal(authentication)),
            staticContentUrlResolver));
  }

  /**
   * {@code deleteBotProfile}은 데이터를 삭제하거나 무효화합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @DeleteMapping("/bot-profiles/{botProfileId}")
  public ResponseEntity<Void> deleteBotProfile(
      @PathVariable Long botProfileId, Authentication authentication) {
    discordBotProfileResourceService.delete(botProfileId, extractPrincipal(authentication));
    return ResponseEntity.noContent().build();
  }

  /**
   * {@code extractPrincipal}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private ChzzkPrincipal extractPrincipal(Authentication authentication) {
    final Object principalObject = authentication.getPrincipal();
    if (principalObject instanceof ChzzkPrincipal chzzkPrincipal) {
      return chzzkPrincipal;
    }

    final AppRole role =
        authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))
            ? AppRole.ADMIN
            : AppRole.USER;
    return new ChzzkPrincipal(authentication.getName(), role);
  }
}
