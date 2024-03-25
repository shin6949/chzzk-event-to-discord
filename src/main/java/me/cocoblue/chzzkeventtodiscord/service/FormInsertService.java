package me.cocoblue.chzzkeventtodiscord.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.data.LanguageIsoData;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataRepository;
import me.cocoblue.chzzkeventtodiscord.dto.FormInsertRequestDto;
import me.cocoblue.chzzkeventtodiscord.dto.FormInsertResponseDto;
import me.cocoblue.chzzkeventtodiscord.service.chzzk.ChzzkChannelService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class FormInsertService<T extends ChzzkSubscriptionFormEntity> {
    private final DiscordWebhookDataRepository discordWebhookDataRepository;
    private final DiscordBotProfileDataRepository discordBotProfileDataRepository;
    private final ChzzkSubscriptionFormService<T> chzzkSubscriptionFormService;
    private final ChzzkChannelService chzzkChannelService;

    @Transactional
    public FormInsertResponseDto insertForm(final FormInsertRequestDto formInsertRequestDTO) {
        log.info("Form insert request: {}", formInsertRequestDTO);

        final ChzzkChannelEntity requestedChannelEntity = resolveChannelEntity(formInsertRequestDTO.getChannelId(), formInsertRequestDTO.getChannelName(), false);
        final ChzzkChannelEntity ownerChannelEntity = resolveChannelEntity(formInsertRequestDTO.getOwnerChannelId(), formInsertRequestDTO.getOwnerChannelName(), true);

        final T requestForm = buildFormEntity(formInsertRequestDTO, requestedChannelEntity, ownerChannelEntity);

        final DiscordWebhookDataEntity webhookEntity = resolveWebhookEntity(formInsertRequestDTO, ownerChannelEntity);
        requestForm.setWebhookId(webhookEntity);

        final DiscordBotProfileDataEntity botProfileEntity = resolveBotProfileEntity(formInsertRequestDTO, ownerChannelEntity);
        requestForm.setBotProfileId(botProfileEntity);

        chzzkSubscriptionFormService.save(requestForm);
        return buildFormInsertResponseDTO(requestForm, webhookEntity, botProfileEntity);
    }

    private ChzzkChannelEntity resolveChannelEntity(String channelId, String channelName, boolean isOwner) {
        if (channelId == null && channelName != null) {
            log.info("Requested {} Channel name: {}", isOwner ? "Owner" : "Channel", channelName);
            channelId = chzzkChannelService.getChannelByChannelName(channelName).getChannelId();
        } else if(channelId != null && channelName == null) {
            chzzkChannelService.getChannelByChannelId(channelId);
        }
        return chzzkChannelService.getChannelEntityByChannelIdFromDatabase(channelId);
    }

    private DiscordWebhookDataEntity resolveWebhookEntity(FormInsertRequestDto dto, ChzzkChannelEntity owner) {
        return Optional.ofNullable(dto.getWebhookId())
                .map(id -> discordWebhookDataRepository.findById(id).orElseThrow())
                .orElseGet(() -> createOrGetExistingWebhook(dto, owner));
    }

    private DiscordWebhookDataEntity createOrGetExistingWebhook(FormInsertRequestDto dto, ChzzkChannelEntity owner) {
        return discordWebhookDataRepository.findDiscordWebhookDataEntityByWebhookUrlAndNameAndOwnerId(dto.getWebhookUrl(), dto.getWebhookName(), owner)
                .orElseGet(() -> {
                    log.info("Webhook does not exist. Creating new webhook");
                    DiscordWebhookDataEntity newWebhook = DiscordWebhookDataEntity.builder()
                            .ownerId(owner)
                            .name(dto.getWebhookName())
                            .webhookUrl(dto.getWebhookUrl())
                            .build();
                    return discordWebhookDataRepository.save(newWebhook);
                });
    }

    private DiscordBotProfileDataEntity resolveBotProfileEntity(FormInsertRequestDto dto, ChzzkChannelEntity owner) {
        return Optional.ofNullable(dto.getBotProfileId())
                .map(id -> discordBotProfileDataRepository.findById(id).orElseThrow())
                .orElseGet(() -> createOrGetExistingBotProfile(dto, owner));
    }

    private DiscordBotProfileDataEntity createOrGetExistingBotProfile(FormInsertRequestDto dto, ChzzkChannelEntity owner) {
        return discordBotProfileDataRepository.findDiscordBotProfileDataEntityByAvatarUrlAndOwnerIdAndUsername(dto.getBotAvatarUrl(), owner, dto.getBotUsername())
                .orElseGet(() -> {
                    log.info("Bot profile does not exist. Creating new bot profile");
                    DiscordBotProfileDataEntity newBotProfile = DiscordBotProfileDataEntity.builder()
                            .avatarUrl(dto.getBotAvatarUrl())
                            .username(dto.getBotUsername())
                            .ownerId(owner)
                            .build();
                    return discordBotProfileDataRepository.save(newBotProfile);
                });
    }

    private ChzzkSubscriptionFormEntity buildFormEntity(final FormInsertRequestDto formInsertRequestDto,
                                                        final ChzzkChannelEntity channel,
                                                        final ChzzkChannelEntity owner) {
        final ChzzkSubscriptionFormEntity entity = ChzzkSubscriptionFormEntity.builder()
                        .chzzkChannelEntity(channel)
                        .formOwner(owner)
                        .content(formInsertRequestDto.getContent())
                        .chzzkSubscriptionType(formInsertRequestDto.getSubscriptionType())
                        .enabled(formInsertRequestDto.getEnabled() == null || formInsertRequestDto.getEnabled())
                        .intervalMinute(formInsertRequestDto.getIntervalMinute() == null ? 10 : formInsertRequestDto.getIntervalMinute())
                        .languageIsoData(formInsertRequestDto.getLanguage() == null ? LanguageIsoData.Korean : formInsertRequestDto.getLanguage())
                        .colorHex(formInsertRequestDto.getColorHex() == null ? "000000" : formInsertRequestDto.getColorHex())
                        .build();
        // TODO: 반영 필요
//        entity.setShowDetail(dto.getShowDetail() == null || dto.getShowDetail());
        return (T) entity;
    }

    private FormInsertResponseDto buildFormInsertResponseDTO(final ChzzkSubscriptionFormEntity form,
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
}
