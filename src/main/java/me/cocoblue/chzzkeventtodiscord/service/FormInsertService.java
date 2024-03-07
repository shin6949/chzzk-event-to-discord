package me.cocoblue.chzzkeventtodiscord.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.data.LanguageIsoData;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataRepository;
import me.cocoblue.chzzkeventtodiscord.dto.FormInsertRequestDTO;
import me.cocoblue.chzzkeventtodiscord.dto.FormInsertResponseDTO;
import me.cocoblue.chzzkeventtodiscord.service.chzzk.ChzzkChannelService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class FormInsertService {
    private final DiscordWebhookDataRepository discordWebhookDataRepository;
    private final DiscordBotProfileDataRepository discordBotProfileDataRepository;
    private final ChzzkSubscriptionFormService chzzkSubscriptionFormService;
    private final ChzzkChannelService chzzkChannelService;

    @Transactional
    public FormInsertResponseDTO insertForm(final FormInsertRequestDTO formInsertRequestDTO) {
        log.info("Form insert request: {}", formInsertRequestDTO);

        final ChzzkChannelEntity requestedChannelEntity = resolveChannelEntity(formInsertRequestDTO.getChannelId(), formInsertRequestDTO.getChannelName(), false);
        final ChzzkChannelEntity ownerChannelEntity = resolveChannelEntity(formInsertRequestDTO.getOwnerChannelId(), formInsertRequestDTO.getOwnerChannelName(), true);

        final ChzzkSubscriptionFormEntity requestForm = buildFormEntity(formInsertRequestDTO, requestedChannelEntity, ownerChannelEntity);

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

    private DiscordWebhookDataEntity resolveWebhookEntity(FormInsertRequestDTO dto, ChzzkChannelEntity owner) {
        return Optional.ofNullable(dto.getWebhookId())
                .map(id -> discordWebhookDataRepository.findById(id).orElseThrow())
                .orElseGet(() -> createOrGetExistingWebhook(dto, owner));
    }

    private DiscordWebhookDataEntity createOrGetExistingWebhook(FormInsertRequestDTO dto, ChzzkChannelEntity owner) {
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

    private DiscordBotProfileDataEntity resolveBotProfileEntity(FormInsertRequestDTO dto, ChzzkChannelEntity owner) {
        return Optional.ofNullable(dto.getBotProfileId())
                .map(id -> discordBotProfileDataRepository.findById(id).orElseThrow())
                .orElseGet(() -> createOrGetExistingBotProfile(dto, owner));
    }

    private DiscordBotProfileDataEntity createOrGetExistingBotProfile(FormInsertRequestDTO dto, ChzzkChannelEntity owner) {
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

    private ChzzkSubscriptionFormEntity buildFormEntity(FormInsertRequestDTO dto, ChzzkChannelEntity channel, ChzzkChannelEntity owner) {
        ChzzkSubscriptionFormEntity entity = new ChzzkSubscriptionFormEntity();
        entity.setChzzkChannelEntity(channel);
        entity.setFormOwner(owner);
        entity.setContent(dto.getContent());
        entity.setChzzkSubscriptionType(dto.getSubscriptionType());
        entity.setEnabled(dto.getEnabled() == null || dto.getEnabled());
        entity.setIntervalMinute(dto.getIntervalMinute() == null ? 10 : dto.getIntervalMinute());
        entity.setLanguageIsoData(dto.getLanguage() == null ? LanguageIsoData.Korean : dto.getLanguage());
        entity.setShowDetail(dto.getShowDetail() == null || dto.getShowDetail());
        entity.setColorHex(dto.getColorHex() == null ? "000000" : dto.getColorHex());
        return entity;
    }

    private FormInsertResponseDTO buildFormInsertResponseDTO(ChzzkSubscriptionFormEntity form, DiscordWebhookDataEntity webhook, DiscordBotProfileDataEntity botProfile) {
        FormInsertResponseDTO dto = new FormInsertResponseDTO();
        dto.setIsSuccess(true);
        dto.setRegisteredFormId(form.getId());
        dto.setRegisteredWebhookId(webhook.getId());
        dto.setRegisteredBotProfileId(botProfile.getId());
        log.info("Form inserted");
        return dto;
    }
}
