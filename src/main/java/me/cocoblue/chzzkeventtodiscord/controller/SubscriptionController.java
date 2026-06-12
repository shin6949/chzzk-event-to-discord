package me.cocoblue.chzzkeventtodiscord.controller;

import jakarta.validation.Valid;
import java.time.ZonedDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;
import me.cocoblue.chzzkeventtodiscord.dto.PageResponseDto;
import me.cocoblue.chzzkeventtodiscord.dto.subscription.SubscriptionRequestDto;
import me.cocoblue.chzzkeventtodiscord.dto.subscription.SubscriptionResponseDto;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import me.cocoblue.chzzkeventtodiscord.service.NotificationLogService;
import me.cocoblue.chzzkeventtodiscord.service.SubscriptionCrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code SubscriptionController}는 HTTP API 요청을 받아 서비스 계층으로 위임합니다.
 *
 * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 7fec3f1.
 *
 * @since unreleased after Ver.0.1.4
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
  private final SubscriptionCrudService subscriptionCrudService;
  private final NotificationLogService notificationLogService;

  /**
   * {@code create}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @PostMapping
  public ResponseEntity<SubscriptionResponseDto> create(
      @Valid @RequestBody SubscriptionRequestDto request, Authentication authentication) {
    final ChzzkPrincipal principal = extractPrincipal(authentication);
    final SubscriptionResponseDto response =
        SubscriptionResponseDto.fromEntity(subscriptionCrudService.create(request, principal));

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * {@code list}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @GetMapping
  public ResponseEntity<PageResponseDto<SubscriptionResponseDto>> list(
      Pageable pageable, Authentication authentication) {
    final ChzzkPrincipal principal = extractPrincipal(authentication);
    final Page<ChzzkSubscriptionFormEntity> subscriptions =
        subscriptionCrudService.list(principal, pageable);
    final Map<Long, ZonedDateTime> lastNotificationSentAtBySubscriptionId =
        notificationLogService.findLastNotificationSentAtBySubscriptionIds(
            subscriptions.getContent().stream().map(ChzzkSubscriptionFormEntity::getId).toList());
    final Page<SubscriptionResponseDto> response =
        subscriptions.map(
            subscription ->
                SubscriptionResponseDto.fromEntity(
                    subscription,
                    lastNotificationSentAtBySubscriptionId.get(subscription.getId())));

    return ResponseEntity.ok(PageResponseDto.from(response));
  }

  /**
   * {@code get}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @GetMapping("/{subscriptionId}")
  public ResponseEntity<SubscriptionResponseDto> get(
      @PathVariable Long subscriptionId, Authentication authentication) {
    final ChzzkPrincipal principal = extractPrincipal(authentication);
    final SubscriptionResponseDto response =
        SubscriptionResponseDto.fromEntity(subscriptionCrudService.get(subscriptionId, principal));

    return ResponseEntity.ok(response);
  }

  /**
   * {@code update}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @PutMapping("/{subscriptionId}")
  public ResponseEntity<SubscriptionResponseDto> update(
      @PathVariable Long subscriptionId,
      @Valid @RequestBody SubscriptionRequestDto request,
      Authentication authentication) {
    final ChzzkPrincipal principal = extractPrincipal(authentication);
    final SubscriptionResponseDto response =
        SubscriptionResponseDto.fromEntity(
            subscriptionCrudService.update(subscriptionId, request, principal));

    return ResponseEntity.ok(response);
  }

  /**
   * {@code delete}은 데이터를 삭제하거나 무효화합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @DeleteMapping("/{subscriptionId}")
  public ResponseEntity<Void> delete(
      @PathVariable Long subscriptionId, Authentication authentication) {
    final ChzzkPrincipal principal = extractPrincipal(authentication);
    subscriptionCrudService.delete(subscriptionId, principal);
    return ResponseEntity.noContent().build();
  }

  /**
   * {@code extractPrincipal}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
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
