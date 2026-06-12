package me.cocoblue.chzzkeventtodiscord.service;

import jakarta.transaction.Transactional;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.data.LanguageIsoData;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkSubscriptionType;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkStreamOnlineFormEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataRepository;
import me.cocoblue.chzzkeventtodiscord.dto.subscription.SubscriptionRequestDto;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * {@code SubscriptionCrudService}는 도메인 비즈니스 로직을 수행합니다.
 *
 * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 7fec3f1.
 *
 * @since unreleased after Ver.0.1.4
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class SubscriptionCrudService {
  private static final int DEFAULT_INTERVAL_MINUTE = 10;
  private static final String DEFAULT_COLOR_HEX = "000000";

  private final ChzzkSubscriptionFormRepository subscriptionFormRepository;
  private final ChzzkChannelRepository chzzkChannelRepository;
  private final DiscordWebhookDataRepository discordWebhookDataRepository;
  private final DiscordBotProfileDataRepository discordBotProfileDataRepository;

  /**
   * {@code create}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Transactional
  public ChzzkSubscriptionFormEntity create(
      SubscriptionRequestDto request, ChzzkPrincipal principal) {
    final String ownerChannelId =
        resolveOwnerChannelId(request.getFormOwnerChannelId(), principal, false);
    final String targetChannelId = resolveTargetChannelId(request.getChannelId(), principal, null);
    final ChzzkChannelEntity targetChannel = resolveChannel(targetChannelId, "channelId");
    final ChzzkChannelEntity ownerChannel = resolveChannel(ownerChannelId, "formOwnerChannelId");
    final DiscordWebhookDataEntity webhook =
        resolveWebhook(request.getWebhookId(), ownerChannelId, principal);
    final DiscordBotProfileDataEntity botProfile =
        resolveBotProfile(request.getBotProfileId(), ownerChannelId, principal);
    final ChzzkSubscriptionType subscriptionType =
        requireSubscriptionType(request.getSubscriptionType());

    final ChzzkSubscriptionFormEntity entity;
    if (subscriptionType == ChzzkSubscriptionType.STREAM_ONLINE) {
      entity =
          ChzzkStreamOnlineFormEntity.builder()
              .showDetail(Boolean.TRUE.equals(request.getShowDetail()))
              .showThumbnail(request.getShowThumbnail() == null || request.getShowThumbnail())
              .showViewerCount(Boolean.TRUE.equals(request.getShowViewerCount()))
              .showTag(request.getShowTag() == null || request.getShowTag())
              .build();
    } else {
      entity = new ChzzkSubscriptionFormEntity();
    }

    applyCommonFields(
        entity, request, targetChannel, ownerChannel, webhook, botProfile, subscriptionType, false);
    return subscriptionFormRepository.saveAndFlush(entity);
  }

  /**
   * {@code list}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Transactional
  public Page<ChzzkSubscriptionFormEntity> list(ChzzkPrincipal principal, Pageable pageable) {
    if (principal.role() == AppRole.ADMIN) {
      return subscriptionFormRepository.findAll(pageable);
    }
    return subscriptionFormRepository.findAllByFormOwner_ChannelId(principal.channelId(), pageable);
  }

  /**
   * {@code get}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Transactional
  public ChzzkSubscriptionFormEntity get(Long id, ChzzkPrincipal principal) {
    final ChzzkSubscriptionFormEntity entity = findById(id);
    ensureReadable(entity, principal);
    return entity;
  }

  /**
   * {@code update}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Transactional
  public ChzzkSubscriptionFormEntity update(
      Long id, SubscriptionRequestDto request, ChzzkPrincipal principal) {
    final ChzzkSubscriptionFormEntity entity = findById(id);
    ensureWritable(entity, principal);

    final ChzzkSubscriptionType nextType =
        request.getSubscriptionType() == null
            ? entity.getChzzkSubscriptionType()
            : request.getSubscriptionType();
    ensureTypeIsNotMutatedAcrossHierarchy(entity, nextType);

    final String ownerChannelId =
        resolveOwnerChannelId(request.getFormOwnerChannelId(), principal, true);
    final String targetChannelId =
        resolveTargetChannelId(request.getChannelId(), principal, entity);
    final ChzzkChannelEntity targetChannel =
        targetChannelId == null
            ? entity.getChzzkChannelEntity()
            : resolveChannel(targetChannelId, "channelId");
    final ChzzkChannelEntity ownerChannel =
        ownerChannelId == null
            ? entity.getFormOwner()
            : resolveChannel(ownerChannelId, "formOwnerChannelId");
    final DiscordWebhookDataEntity webhook =
        request.getWebhookId() == null
            ? entity.getWebhookId()
            : resolveWebhook(request.getWebhookId(), ownerChannelId, principal);
    final DiscordBotProfileDataEntity botProfile =
        request.getBotProfileId() == null
            ? entity.getBotProfileId()
            : resolveBotProfile(request.getBotProfileId(), ownerChannelId, principal);

    applyCommonFields(
        entity, request, targetChannel, ownerChannel, webhook, botProfile, nextType, true);
    if (entity instanceof ChzzkStreamOnlineFormEntity streamOnlineEntity) {
      if (request.getShowDetail() != null) {
        streamOnlineEntity.setShowDetail(request.getShowDetail());
      }
      if (request.getShowThumbnail() != null) {
        streamOnlineEntity.setShowThumbnail(request.getShowThumbnail());
      }
      if (request.getShowViewerCount() != null) {
        streamOnlineEntity.setShowViewerCount(request.getShowViewerCount());
      }
      if (request.getShowTag() != null) {
        streamOnlineEntity.setShowTag(request.getShowTag());
      }
    }

    return subscriptionFormRepository.saveAndFlush(entity);
  }

  /**
   * {@code delete}은 데이터를 삭제하거나 무효화합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Transactional
  public void delete(Long id, ChzzkPrincipal principal) {
    final ChzzkSubscriptionFormEntity entity = findById(id);
    ensureWritable(entity, principal);
    subscriptionFormRepository.delete(entity);
  }

  /**
   * {@code findById}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private ChzzkSubscriptionFormEntity findById(Long id) {
    return subscriptionFormRepository
        .findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "subscription not found"));
  }

  /**
   * {@code ensureReadable}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private void ensureReadable(ChzzkSubscriptionFormEntity entity, ChzzkPrincipal principal) {
    if (principal.role() == AppRole.ADMIN) {
      return;
    }
    if (!Objects.equals(entity.getFormOwner().getChannelId(), principal.channelId())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "subscription not owned by authenticated user");
    }
  }

  /**
   * {@code ensureWritable}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private void ensureWritable(ChzzkSubscriptionFormEntity entity, ChzzkPrincipal principal) {
    ensureReadable(entity, principal);
  }

  /**
   * {@code resolveOwnerChannelId}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private String resolveOwnerChannelId(
      String requestedOwnerChannelId, ChzzkPrincipal principal, boolean isUpdate) {
    if (principal.role() == AppRole.ADMIN) {
      if (StringUtils.hasText(requestedOwnerChannelId)) {
        return requestedOwnerChannelId;
      }
      if (isUpdate) {
        return null;
      }
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "formOwnerChannelId is required");
    }

    if (StringUtils.hasText(requestedOwnerChannelId)
        && !Objects.equals(requestedOwnerChannelId, principal.channelId())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "formOwnerChannelId does not match authenticated user");
    }
    return principal.channelId();
  }

  /**
   * {@code resolveTargetChannelId}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private String resolveTargetChannelId(
      String requestedChannelId,
      ChzzkPrincipal principal,
      ChzzkSubscriptionFormEntity existingEntity) {
    if (principal.role() == AppRole.ADMIN) {
      if (StringUtils.hasText(requestedChannelId)) {
        return requestedChannelId;
      }
      if (existingEntity != null) {
        return null;
      }
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "channelId is required");
    }

    if (StringUtils.hasText(requestedChannelId)
        && !Objects.equals(requestedChannelId, principal.channelId())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "channelId does not match authenticated user");
    }
    return principal.channelId();
  }

  /**
   * {@code resolveChannel}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private ChzzkChannelEntity resolveChannel(String channelId, String fieldName) {
    if (!StringUtils.hasText(channelId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
    }
    return chzzkChannelRepository
        .findById(channelId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, fieldName + " not found"));
  }

  /**
   * {@code resolveWebhook}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private DiscordWebhookDataEntity resolveWebhook(
      Long webhookId, String ownerChannelId, ChzzkPrincipal principal) {
    if (webhookId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "webhookId is required");
    }

    final DiscordWebhookDataEntity webhook =
        discordWebhookDataRepository
            .findById(webhookId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "webhook not found"));
    ensureRelatedResourceOwner(
        webhook.getOwnerId().getChannelId(), ownerChannelId, principal, "webhook");
    return webhook;
  }

  /**
   * {@code resolveBotProfile}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private DiscordBotProfileDataEntity resolveBotProfile(
      Long botProfileId, String ownerChannelId, ChzzkPrincipal principal) {
    if (botProfileId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "botProfileId is required");
    }

    final DiscordBotProfileDataEntity botProfile =
        discordBotProfileDataRepository
            .findById(botProfileId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "botProfile not found"));
    ensureRelatedResourceOwner(
        botProfile.getOwnerId().getChannelId(), ownerChannelId, principal, "botProfile");
    return botProfile;
  }

  /**
   * {@code ensureRelatedResourceOwner}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private void ensureRelatedResourceOwner(
      String actualOwnerChannelId,
      String expectedOwnerChannelId,
      ChzzkPrincipal principal,
      String resourceName) {
    if (principal.role() == AppRole.ADMIN) {
      return;
    }
    if (!Objects.equals(actualOwnerChannelId, expectedOwnerChannelId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, resourceName + " does not belong to authenticated owner");
    }
  }

  /**
   * {@code requireSubscriptionType}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private ChzzkSubscriptionType requireSubscriptionType(ChzzkSubscriptionType subscriptionType) {
    if (subscriptionType == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "subscriptionType is required");
    }
    return subscriptionType;
  }

  /**
   * {@code ensureTypeIsNotMutatedAcrossHierarchy}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private void ensureTypeIsNotMutatedAcrossHierarchy(
      ChzzkSubscriptionFormEntity entity, ChzzkSubscriptionType nextType) {
    final boolean currentlyStreamOnline = entity instanceof ChzzkStreamOnlineFormEntity;
    final boolean requestedStreamOnline = nextType == ChzzkSubscriptionType.STREAM_ONLINE;
    if (currentlyStreamOnline != requestedStreamOnline) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "subscriptionType cannot move between STREAM_ONLINE and non-STREAM_ONLINE");
    }
  }

  /**
   * {@code applyCommonFields}은 관련 처리 흐름을 실행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private void applyCommonFields(
      ChzzkSubscriptionFormEntity entity,
      SubscriptionRequestDto request,
      ChzzkChannelEntity targetChannel,
      ChzzkChannelEntity ownerChannel,
      DiscordWebhookDataEntity webhook,
      DiscordBotProfileDataEntity botProfile,
      ChzzkSubscriptionType subscriptionType,
      boolean keepExistingOnNull) {
    entity.setChzzkChannelEntity(targetChannel);
    entity.setFormOwner(ownerChannel);
    entity.setWebhookId(webhook);
    entity.setBotProfileId(botProfile);
    entity.setChzzkSubscriptionType(subscriptionType);
    entity.setIntervalMinute(
        resolveInterval(
            request.getIntervalMinute(), entity.getIntervalMinute(), keepExistingOnNull));
    entity.setLanguageIsoData(
        resolveLanguage(request.getLanguage(), entity.getLanguageIsoData(), keepExistingOnNull));
    entity.setEnabled(resolveEnabled(request.getEnabled(), entity.isEnabled(), keepExistingOnNull));
    entity.setColorHex(
        resolveColorHex(request.getColorHex(), entity.getColorHex(), keepExistingOnNull));
    entity.setContent(
        keepExistingOnNull && request.getContent() == null
            ? entity.getContent()
            : request.getContent());
  }

  /**
   * {@code resolveInterval}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private int resolveInterval(Integer requestValue, int existingValue, boolean keepExistingOnNull) {
    if (requestValue != null) {
      return requestValue;
    }
    if (keepExistingOnNull) {
      return existingValue;
    }
    return DEFAULT_INTERVAL_MINUTE;
  }

  /**
   * {@code resolveLanguage}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private LanguageIsoData resolveLanguage(
      LanguageIsoData requestValue, LanguageIsoData existingValue, boolean keepExistingOnNull) {
    if (requestValue != null) {
      return requestValue;
    }
    if (keepExistingOnNull && existingValue != null) {
      return existingValue;
    }
    return LanguageIsoData.Korean;
  }

  /**
   * {@code resolveEnabled}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private boolean resolveEnabled(
      Boolean requestValue, boolean existingValue, boolean keepExistingOnNull) {
    if (requestValue != null) {
      return requestValue;
    }
    if (keepExistingOnNull) {
      return existingValue;
    }
    return true;
  }

  /**
   * {@code resolveColorHex}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private String resolveColorHex(
      String requestValue, String existingValue, boolean keepExistingOnNull) {
    if (StringUtils.hasText(requestValue)) {
      return requestValue;
    }
    if (keepExistingOnNull && StringUtils.hasText(existingValue)) {
      return existingValue;
    }
    return DEFAULT_COLOR_HEX;
  }
}
