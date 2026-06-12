package me.cocoblue.chzzkeventtodiscord.service;

import jakarta.transaction.Transactional;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.data.LanguageIsoData;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkStreamOnlineFormEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataRepository;
import me.cocoblue.chzzkeventtodiscord.dto.FormInsertRequestDto;
import me.cocoblue.chzzkeventtodiscord.dto.FormInsertResponseDto;
import me.cocoblue.chzzkeventtodiscord.security.DiscordWebhookUrlPolicy;
import me.cocoblue.chzzkeventtodiscord.security.SecretEncryptionService;
import me.cocoblue.chzzkeventtodiscord.service.chzzk.ChzzkChannelService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * {@code FormInsertService}는 도메인 비즈니스 로직을 수행합니다.
 *
 * <p>Git 이력: 생성 2024-03-04 02:28:28 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 db09ddc.
 *
 * @since Ver.0.1
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class FormInsertService {
  private final DiscordWebhookDataRepository discordWebhookDataRepository;
  private final DiscordBotProfileDataRepository discordBotProfileDataRepository;
  private final ChzzkSubscriptionFormService chzzkSubscriptionFormService;
  private final ChzzkStreamOnlineFormService chzzkStreamOnlineFormService;
  private final ChzzkChannelService chzzkChannelService;
  private final DiscordWebhookUrlPolicy discordWebhookUrlPolicy;
  private final SecretEncryptionService secretEncryptionService;

  /**
   * {@code insertForm}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2024-03-04 02:28:28 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 db09ddc.
   *
   * @since Ver.0.1
   */
  @Transactional
  public FormInsertResponseDto insertForm(final FormInsertRequestDto formInsertRequestDto) {
    log.info("Form insert request received.");

    final ChzzkChannelEntity requestedChannelEntity =
        resolveChannelEntity(
            formInsertRequestDto.getChannelId(), formInsertRequestDto.getChannelName(), false);
    final ChzzkChannelEntity ownerChannelEntity =
        resolveChannelEntity(
            formInsertRequestDto.getOwnerChannelId(),
            formInsertRequestDto.getOwnerChannelName(),
            true);

    final DiscordWebhookDataEntity webhookEntity =
        resolveWebhookEntity(formInsertRequestDto, ownerChannelEntity);
    final DiscordBotProfileDataEntity botProfileEntity =
        resolveBotProfileEntity(formInsertRequestDto, ownerChannelEntity);

    switch (formInsertRequestDto.getSubscriptionType()) {
      case STREAM_ONLINE -> {
        final ChzzkStreamOnlineFormEntity requestForm =
            buildStreamOnlineFormEntity(
                formInsertRequestDto, requestedChannelEntity, ownerChannelEntity);
        requestForm.setWebhookId(webhookEntity);
        requestForm.setBotProfileId(botProfileEntity);
        chzzkStreamOnlineFormService.save(requestForm);
        return buildFormInsertResponseDTO(requestForm, webhookEntity, botProfileEntity);
      }

      default -> {
        final ChzzkSubscriptionFormEntity requestForm =
            buildFormEntity(formInsertRequestDto, requestedChannelEntity, ownerChannelEntity);
        requestForm.setWebhookId(webhookEntity);
        requestForm.setBotProfileId(botProfileEntity);
        chzzkSubscriptionFormService.save(requestForm);
        return buildFormInsertResponseDTO(requestForm, webhookEntity, botProfileEntity);
      }
    }
  }

  /**
   * {@code resolveChannelEntity}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-03-05 20:28:48 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.1, 근거 커밋 742a362.
   *
   * @since Ver.0.1.1
   */
  private ChzzkChannelEntity resolveChannelEntity(
      String channelId, final String channelName, boolean isOwner) {
    if (!StringUtils.hasText(channelId) && !StringUtils.hasText(channelName)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          (isOwner ? "ownerChannelId" : "channelId") + " or channelName is required");
    }
    if (StringUtils.hasText(channelId)) {
      channelId = channelId.trim();
    }
    if (!StringUtils.hasText(channelId) && StringUtils.hasText(channelName)) {
      final String trimmedChannelName = channelName.trim();
      log.info("Requested {} Channel name: {}", isOwner ? "Owner" : "Channel", trimmedChannelName);
      channelId = chzzkChannelService.getChannelByChannelName(trimmedChannelName).getChannelId();
    } else if (StringUtils.hasText(channelId) && !StringUtils.hasText(channelName)) {
      chzzkChannelService.getChannelByChannelId(channelId);
    }
    return chzzkChannelService.getChannelEntityByChannelIdFromDatabase(channelId);
  }

  /**
   * {@code resolveWebhookEntity}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-03-05 20:28:48 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.1, 근거 커밋 742a362.
   *
   * @since Ver.0.1.1
   */
  private DiscordWebhookDataEntity resolveWebhookEntity(
      FormInsertRequestDto dto, ChzzkChannelEntity owner) {
    return Optional.ofNullable(dto.getWebhookId())
        .map(id -> findOwnedWebhook(id, owner))
        .orElseGet(() -> createOrGetExistingWebhook(dto, owner));
  }

  /**
   * {@code findOwnedWebhook}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private DiscordWebhookDataEntity findOwnedWebhook(Long id, ChzzkChannelEntity owner) {
    final DiscordWebhookDataEntity webhook =
        discordWebhookDataRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "webhook not found"));
    if (!Objects.equals(webhook.getOwnerId().getChannelId(), owner.getChannelId())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "webhook does not belong to owner channel");
    }
    return webhook;
  }

  /**
   * {@code createOrGetExistingWebhook}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2024-03-05 20:28:48 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.1, 근거 커밋 742a362.
   *
   * @since Ver.0.1.1
   */
  private DiscordWebhookDataEntity createOrGetExistingWebhook(
      FormInsertRequestDto dto, ChzzkChannelEntity owner) {
    return discordWebhookDataRepository
        .findDiscordWebhookDataEntityByWebhookUrlAndNameAndOwnerId(
            dto.getWebhookUrl(), dto.getWebhookName(), owner)
        .orElseGet(
            () -> {
              log.info("Webhook does not exist. Creating new webhook");
              DiscordWebhookDataEntity newWebhook =
                  DiscordWebhookDataEntity.builder()
                      .ownerId(owner)
                      .name(dto.getWebhookName())
                      .webhookUrl(
                          secretEncryptionService.encrypt(
                              discordWebhookUrlPolicy.requireValid(dto.getWebhookUrl())))
                      .build();
              return discordWebhookDataRepository.save(newWebhook);
            });
  }

  /**
   * {@code resolveBotProfileEntity}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-03-05 20:28:48 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.1, 근거 커밋 742a362.
   *
   * @since Ver.0.1.1
   */
  private DiscordBotProfileDataEntity resolveBotProfileEntity(
      FormInsertRequestDto dto, ChzzkChannelEntity owner) {
    return Optional.ofNullable(dto.getBotProfileId())
        .map(id -> findOwnedBotProfile(id, owner))
        .orElseGet(() -> createOrGetExistingBotProfile(dto, owner));
  }

  /**
   * {@code findOwnedBotProfile}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private DiscordBotProfileDataEntity findOwnedBotProfile(Long id, ChzzkChannelEntity owner) {
    final DiscordBotProfileDataEntity botProfile =
        discordBotProfileDataRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "botProfile not found"));
    if (!Objects.equals(botProfile.getOwnerId().getChannelId(), owner.getChannelId())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "botProfile does not belong to owner channel");
    }
    return botProfile;
  }

  /**
   * {@code createOrGetExistingBotProfile}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2024-03-05 20:28:48 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.1, 근거 커밋 742a362.
   *
   * @since Ver.0.1.1
   */
  private DiscordBotProfileDataEntity createOrGetExistingBotProfile(
      FormInsertRequestDto dto, ChzzkChannelEntity owner) {
    return discordBotProfileDataRepository
        .findDiscordBotProfileDataEntityByAvatarUrlAndOwnerIdAndUsername(
            dto.getBotAvatarUrl(), owner, dto.getBotUsername())
        .orElseGet(
            () -> {
              log.info("Bot profile does not exist. Creating new bot profile");
              DiscordBotProfileDataEntity newBotProfile =
                  DiscordBotProfileDataEntity.builder()
                      .avatarUrl(requireExternalHttpsUrl(dto.getBotAvatarUrl(), "botAvatarUrl"))
                      .username(dto.getBotUsername())
                      .alias(dto.getBotUsername())
                      .ownerId(owner)
                      .build();
              return discordBotProfileDataRepository.save(newBotProfile);
            });
  }

  /**
   * {@code buildFormEntity}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2024-03-05 20:28:48 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.1, 근거 커밋 742a362.
   *
   * @since Ver.0.1.1
   */
  private ChzzkSubscriptionFormEntity buildFormEntity(
      final FormInsertRequestDto formInsertRequestDto,
      final ChzzkChannelEntity channel,
      final ChzzkChannelEntity owner) {
    return ChzzkSubscriptionFormEntity.builder()
        .chzzkChannelEntity(channel)
        .formOwner(owner)
        .content(formInsertRequestDto.getContent())
        .chzzkSubscriptionType(formInsertRequestDto.getSubscriptionType())
        .enabled(formInsertRequestDto.getEnabled() == null || formInsertRequestDto.getEnabled())
        .intervalMinute(
            formInsertRequestDto.getIntervalMinute() == null
                ? 10
                : formInsertRequestDto.getIntervalMinute())
        .languageIsoData(
            formInsertRequestDto.getLanguage() == null
                ? LanguageIsoData.Korean
                : formInsertRequestDto.getLanguage())
        .colorHex(
            formInsertRequestDto.getColorHex() == null
                ? "000000"
                : formInsertRequestDto.getColorHex())
        .build();
  }

  /**
   * {@code buildStreamOnlineFormEntity}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2024-03-26 17:00:14 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.3, 근거 커밋 ebaedcd.
   *
   * @since Ver.0.1.3
   */
  private ChzzkStreamOnlineFormEntity buildStreamOnlineFormEntity(
      final FormInsertRequestDto formInsertRequestDto,
      final ChzzkChannelEntity channel,
      final ChzzkChannelEntity owner) {

    return ChzzkStreamOnlineFormEntity.builder()
        .chzzkChannelEntity(channel)
        .formOwner(owner)
        .content(formInsertRequestDto.getContent())
        .showDetail(
            formInsertRequestDto.getShowDetail() == null || formInsertRequestDto.getShowDetail())
        .chzzkSubscriptionType(formInsertRequestDto.getSubscriptionType())
        .enabled(formInsertRequestDto.getEnabled() == null || formInsertRequestDto.getEnabled())
        .intervalMinute(
            formInsertRequestDto.getIntervalMinute() == null
                ? 10
                : formInsertRequestDto.getIntervalMinute())
        .languageIsoData(
            formInsertRequestDto.getLanguage() == null
                ? LanguageIsoData.Korean
                : formInsertRequestDto.getLanguage())
        .colorHex(
            formInsertRequestDto.getColorHex() == null
                ? "000000"
                : formInsertRequestDto.getColorHex())
        .build();
  }

  /**
   * {@code buildFormInsertResponseDTO}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2024-03-05 20:28:48 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.1, 근거 커밋 742a362.
   *
   * @since Ver.0.1.1
   */
  private FormInsertResponseDto buildFormInsertResponseDTO(
      final ChzzkSubscriptionFormEntity form,
      final DiscordWebhookDataEntity webhook,
      final DiscordBotProfileDataEntity botProfile) {
    FormInsertResponseDto dto = new FormInsertResponseDto();
    dto.setIsSuccess(true);
    dto.setRegisteredFormId(form.getId());
    dto.setRegisteredWebhookId(webhook.getId());
    dto.setRegisteredBotProfileId(botProfile.getId());
    log.info("Form inserted");
    return dto;
  }

  /**
   * {@code requireExternalHttpsUrl}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private String requireExternalHttpsUrl(String value, String fieldName) {
    if (!StringUtils.hasText(value)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
    }
    try {
      final URI uri = new URI(value.trim());
      if (!"https".equalsIgnoreCase(uri.getScheme())
          || !StringUtils.hasText(uri.getHost())
          || StringUtils.hasText(uri.getUserInfo())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, fieldName + " must be an https URL");
      }
      return uri.toString();
    } catch (URISyntaxException exception) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, fieldName + " is invalid", exception);
    }
  }
}
