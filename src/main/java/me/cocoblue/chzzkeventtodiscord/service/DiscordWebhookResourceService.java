package me.cocoblue.chzzkeventtodiscord.service;

import jakarta.transaction.Transactional;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataRepository;
import me.cocoblue.chzzkeventtodiscord.dto.discord.DiscordWebhookRequestDto;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import me.cocoblue.chzzkeventtodiscord.security.DiscordWebhookUrlPolicy;
import me.cocoblue.chzzkeventtodiscord.security.SecretEncryptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * {@code DiscordWebhookResourceService}는 도메인 비즈니스 로직을 수행합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
@Service
@RequiredArgsConstructor
public class DiscordWebhookResourceService {
  private final DiscordWebhookDataRepository discordWebhookDataRepository;
  private final ChzzkChannelRepository chzzkChannelRepository;
  private final ChzzkSubscriptionFormRepository subscriptionFormRepository;
  private final DiscordWebhookUrlPolicy discordWebhookUrlPolicy;
  private final SecretEncryptionService secretEncryptionService;

  /**
   * {@code list}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Transactional
  public Page<DiscordWebhookDataEntity> list(ChzzkPrincipal principal, Pageable pageable) {
    if (principal.role() == AppRole.ADMIN) {
      return discordWebhookDataRepository.findAll(pageable);
    }
    return discordWebhookDataRepository.findAllByOwnerId_ChannelId(principal.channelId(), pageable);
  }

  /**
   * {@code get}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Transactional
  public DiscordWebhookDataEntity get(Long id, ChzzkPrincipal principal) {
    final DiscordWebhookDataEntity entity = findReadable(id, principal);
    ensureReadable(entity, principal);
    return entity;
  }

  /**
   * {@code create}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Transactional
  public DiscordWebhookDataEntity create(
      DiscordWebhookRequestDto request, ChzzkPrincipal principal) {
    final String ownerChannelId =
        resolveOwnerChannelId(request.getOwnerChannelId(), principal, false);
    final ChzzkChannelEntity owner = resolveOwner(ownerChannelId);
    final String alias = requireText(request.getAlias(), "alias");
    final String url = encryptWebhookUrl(request.getUrl());

    final DiscordWebhookDataEntity entity =
        DiscordWebhookDataEntity.builder().name(alias).webhookUrl(url).ownerId(owner).build();
    return discordWebhookDataRepository.saveAndFlush(entity);
  }

  /**
   * {@code update}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Transactional
  public DiscordWebhookDataEntity update(
      Long id, DiscordWebhookRequestDto request, ChzzkPrincipal principal) {
    final DiscordWebhookDataEntity entity = findReadable(id, principal);
    ensureWritable(entity, principal);

    if (StringUtils.hasText(request.getAlias())) {
      entity.setName(request.getAlias().trim());
    }
    if (StringUtils.hasText(request.getUrl())) {
      entity.setWebhookUrl(encryptWebhookUrl(request.getUrl()));
    } else if (!secretEncryptionService.isEncrypted(entity.getWebhookUrl())) {
      entity.setWebhookUrl(
          secretEncryptionService.encrypt(
              discordWebhookUrlPolicy.requireValid(entity.getWebhookUrl())));
    }
    return discordWebhookDataRepository.saveAndFlush(entity);
  }

  /**
   * {@code delete}은 데이터를 삭제하거나 무효화합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Transactional
  public void delete(Long id, ChzzkPrincipal principal) {
    final DiscordWebhookDataEntity entity = findReadable(id, principal);
    ensureWritable(entity, principal);
    if (subscriptionFormRepository.existsByWebhookId_Id(id)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "webhook is used by subscriptions");
    }
    discordWebhookDataRepository.delete(entity);
  }

  /**
   * {@code findReadable}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private DiscordWebhookDataEntity findReadable(Long id, ChzzkPrincipal principal) {
    if (principal.role() == AppRole.ADMIN) {
      return discordWebhookDataRepository
          .findById(id)
          .orElseThrow(
              () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "webhook not found"));
    }
    return discordWebhookDataRepository
        .findByIdAndOwnerId_ChannelId(id, principal.channelId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "webhook not found"));
  }

  /**
   * {@code ensureReadable}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private void ensureReadable(DiscordWebhookDataEntity entity, ChzzkPrincipal principal) {
    if (principal.role() == AppRole.ADMIN) {
      return;
    }
    if (!Objects.equals(entity.getOwnerId().getChannelId(), principal.channelId())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "webhook does not belong to authenticated user");
    }
  }

  /**
   * {@code ensureWritable}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private void ensureWritable(DiscordWebhookDataEntity entity, ChzzkPrincipal principal) {
    ensureReadable(entity, principal);
  }

  /**
   * {@code resolveOwnerChannelId}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private String resolveOwnerChannelId(
      String requestedOwnerChannelId, ChzzkPrincipal principal, boolean isUpdate) {
    if (principal.role() == AppRole.ADMIN) {
      if (StringUtils.hasText(requestedOwnerChannelId)) {
        return requestedOwnerChannelId.trim();
      }
      if (isUpdate) {
        return null;
      }
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ownerChannelId is required");
    }
    if (StringUtils.hasText(requestedOwnerChannelId)
        && !Objects.equals(requestedOwnerChannelId.trim(), principal.channelId())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "ownerChannelId does not match authenticated user");
    }
    return principal.channelId();
  }

  /**
   * {@code resolveOwner}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private ChzzkChannelEntity resolveOwner(String ownerChannelId) {
    return chzzkChannelRepository
        .findById(ownerChannelId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "owner channel not found"));
  }

  /**
   * {@code requireText}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private String requireText(String value, String fieldName) {
    if (!StringUtils.hasText(value)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
    }
    return value.trim();
  }

  /**
   * {@code encryptWebhookUrl}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private String encryptWebhookUrl(String value) {
    return secretEncryptionService.encrypt(discordWebhookUrlPolicy.requireValid(value));
  }
}
