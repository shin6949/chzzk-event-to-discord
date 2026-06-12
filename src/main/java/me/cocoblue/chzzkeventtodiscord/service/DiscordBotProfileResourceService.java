package me.cocoblue.chzzkeventtodiscord.service;

import jakarta.transaction.Transactional;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataRepository;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * {@code DiscordBotProfileResourceService}는 도메인 비즈니스 로직을 수행합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
@Service
@RequiredArgsConstructor
public class DiscordBotProfileResourceService {
  private final DiscordBotProfileDataRepository discordBotProfileDataRepository;
  private final ChzzkChannelRepository chzzkChannelRepository;
  private final ChzzkSubscriptionFormRepository subscriptionFormRepository;
  private final BotProfileImageStorageService botProfileImageStorageService;

  /**
   * {@code list}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Transactional
  public Page<DiscordBotProfileDataEntity> list(ChzzkPrincipal principal, Pageable pageable) {
    if (principal.role() == AppRole.ADMIN) {
      return discordBotProfileDataRepository.findAll(pageable);
    }
    return discordBotProfileDataRepository.findAllByOwnerId_ChannelId(
        principal.channelId(), pageable);
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
  public DiscordBotProfileDataEntity get(Long id, ChzzkPrincipal principal) {
    return findReadable(id, principal);
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
  public DiscordBotProfileDataEntity create(
      String alias,
      String username,
      MultipartFile avatar,
      String ownerChannelId,
      ChzzkPrincipal principal) {
    final String resolvedOwnerChannelId = resolveOwnerChannelId(ownerChannelId, principal);
    final ChzzkChannelEntity owner = resolveOwner(resolvedOwnerChannelId);
    final String objectKey = botProfileImageStorageService.upload(resolvedOwnerChannelId, avatar);

    try {
      return discordBotProfileDataRepository.saveAndFlush(
          DiscordBotProfileDataEntity.builder()
              .ownerId(owner)
              .alias(requireText(alias, "alias"))
              .username(requireText(username, "username"))
              .avatarUrl(objectKey)
              .build());
    } catch (RuntimeException exception) {
      botProfileImageStorageService.deleteBestEffort(objectKey);
      throw exception;
    }
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
  public DiscordBotProfileDataEntity update(
      Long id, String alias, String username, MultipartFile avatar, ChzzkPrincipal principal) {
    final DiscordBotProfileDataEntity entity = findReadable(id, principal);
    ensureWritable(entity, principal);

    if (StringUtils.hasText(alias)) {
      entity.setAlias(alias.trim());
    }
    if (StringUtils.hasText(username)) {
      entity.setUsername(username.trim());
    }

    final String previousObjectKey = entity.getAvatarUrl();
    final String nextObjectKey =
        avatar == null || avatar.isEmpty()
            ? null
            : botProfileImageStorageService.upload(entity.getOwnerId().getChannelId(), avatar);

    if (nextObjectKey != null) {
      entity.setAvatarUrl(nextObjectKey);
    }

    try {
      final DiscordBotProfileDataEntity saved =
          discordBotProfileDataRepository.saveAndFlush(entity);
      if (nextObjectKey != null) {
        botProfileImageStorageService.deleteBestEffort(previousObjectKey);
      }
      return saved;
    } catch (RuntimeException exception) {
      if (nextObjectKey != null) {
        botProfileImageStorageService.deleteBestEffort(nextObjectKey);
      }
      throw exception;
    }
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
    final DiscordBotProfileDataEntity entity = findReadable(id, principal);
    ensureWritable(entity, principal);
    if (subscriptionFormRepository.existsByBotProfileId_Id(id)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "bot profile is used by subscriptions");
    }

    discordBotProfileDataRepository.delete(entity);
    botProfileImageStorageService.deleteBestEffort(entity.getAvatarUrl());
  }

  /**
   * {@code findReadable}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private DiscordBotProfileDataEntity findReadable(Long id, ChzzkPrincipal principal) {
    if (principal.role() == AppRole.ADMIN) {
      return discordBotProfileDataRepository
          .findById(id)
          .orElseThrow(
              () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "bot profile not found"));
    }
    return discordBotProfileDataRepository
        .findByIdAndOwnerId_ChannelId(id, principal.channelId())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "bot profile not found"));
  }

  /**
   * {@code ensureWritable}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private void ensureWritable(DiscordBotProfileDataEntity entity, ChzzkPrincipal principal) {
    if (principal.role() == AppRole.ADMIN) {
      return;
    }
    if (!Objects.equals(entity.getOwnerId().getChannelId(), principal.channelId())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "bot profile does not belong to authenticated user");
    }
  }

  /**
   * {@code resolveOwnerChannelId}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private String resolveOwnerChannelId(String requestedOwnerChannelId, ChzzkPrincipal principal) {
    if (principal.role() == AppRole.ADMIN) {
      if (StringUtils.hasText(requestedOwnerChannelId)) {
        return requestedOwnerChannelId.trim();
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
}
