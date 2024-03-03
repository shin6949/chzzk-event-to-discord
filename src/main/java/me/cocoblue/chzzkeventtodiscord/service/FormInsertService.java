package me.cocoblue.chzzkeventtodiscord.service;

import jakarta.persistence.EntityManager;
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
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkChannelDTO;
import me.cocoblue.chzzkeventtodiscord.service.chzzk.ChzzkChannelService;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class FormInsertService {
    private final DiscordWebhookDataRepository discordWebhookDataRepository;
    private final DiscordBotProfileDataRepository discordBotProfileDataRepository;
    private final ChzzkSubscriptionFormService chzzkSubscriptionFormService;
    private final ChzzkChannelService chzzkChannelService;
    private final EntityManager entityManager;

    @Transactional
    public FormInsertResponseDTO insertForm(final FormInsertRequestDTO formInsertRequestDTO) {
        log.info("Form insert request: {}", formInsertRequestDTO);
        final ChzzkSubscriptionFormEntity requestForm = new ChzzkSubscriptionFormEntity();

        if(formInsertRequestDTO.getChannelId() == null && formInsertRequestDTO.getChannelName() != null) {
            log.info("Requested Channel name: {}", formInsertRequestDTO.getChannelName());
            formInsertRequestDTO.setChannelId(chzzkChannelService.getChannelByChannelName(formInsertRequestDTO.getChannelName()).getChannelId());
        } else if(formInsertRequestDTO.getChannelId() != null){
            chzzkChannelService.getChannelByChannelId(formInsertRequestDTO.getChannelId());
        }

        final ChzzkChannelEntity requestedChannelEntity = chzzkChannelService.getChannelEntityByChannelIdFromDatabase(formInsertRequestDTO.getChannelId());
        requestForm.setChzzkChannelEntity(requestedChannelEntity);

        if(formInsertRequestDTO.getOwnerChannelId() == null && formInsertRequestDTO.getOwnerChannelName() != null) {
            log.info("Requested Owner name: {}", formInsertRequestDTO.getOwnerChannelName());
            final ChzzkChannelDTO ownerChannel = chzzkChannelService.getChannelByChannelName(formInsertRequestDTO.getOwnerChannelName());
            formInsertRequestDTO.setOwnerChannelId(ownerChannel.getChannelId());
        }

        final ChzzkChannelEntity ownerChannelEntity = chzzkChannelService.getChannelEntityByChannelIdFromDatabase(formInsertRequestDTO.getOwnerChannelId());
        requestForm.setFormOwner(ownerChannelEntity);

        final FormInsertResponseDTO result = new FormInsertResponseDTO();
        requestForm.setContent(formInsertRequestDTO.getContent());
        requestForm.setChzzkSubscriptionType(formInsertRequestDTO.getSubscriptionType());
        requestForm.setEnabled(formInsertRequestDTO.getEnabled() == null || formInsertRequestDTO.getEnabled());
        requestForm.setIntervalMinute(formInsertRequestDTO.getIntervalMinute() == null ? 10 : formInsertRequestDTO.getIntervalMinute());
        requestForm.setLanguageIsoData(formInsertRequestDTO.getLanguage() == null ? LanguageIsoData.Korean : formInsertRequestDTO.getLanguage());
        requestForm.setColorHex(formInsertRequestDTO.getColorHex() == null ? "000000" : formInsertRequestDTO.getColorHex());

        if(formInsertRequestDTO.getWebhookId() == null) {
            log.info("Webhook id is null. Creating new webhook");
            final DiscordWebhookDataEntity discordWebhookDataEntity = DiscordWebhookDataEntity.builder()
                    .ownerId(ownerChannelEntity)
                    .name(formInsertRequestDTO.getWebhookName())
                    .webhookUrl(formInsertRequestDTO.getWebhookUrl())
                    .meno(null)
                    .build();

            discordWebhookDataRepository.save(discordWebhookDataEntity);
            requestForm.setWebhookId(discordWebhookDataEntity);
            result.setRegisteredWebhookId(discordWebhookDataEntity.getId());
        } else {
            requestForm.setWebhookId(discordWebhookDataRepository.findById(formInsertRequestDTO.getWebhookId()).orElseThrow());
            result.setRegisteredWebhookId(formInsertRequestDTO.getWebhookId());
        }

        if(formInsertRequestDTO.getBotProfileId() == null) {
            final DiscordBotProfileDataEntity discordBotProfileDataEntity = DiscordBotProfileDataEntity.builder()
                    .avatarUrl(formInsertRequestDTO.getBotAvatarUrl())
                    .username(formInsertRequestDTO.getBotUsername())
                    .ownerId(ownerChannelEntity)
                    .build();
            discordBotProfileDataRepository.save(discordBotProfileDataEntity);
            requestForm.setBotProfileId(discordBotProfileDataEntity);
            result.setRegisteredBotProfileId(discordBotProfileDataEntity.getId());
        } else {
            requestForm.setBotProfileId(discordBotProfileDataRepository.findById(formInsertRequestDTO.getBotProfileId()).orElseThrow());
            result.setRegisteredBotProfileId(formInsertRequestDTO.getBotProfileId());
        }

        chzzkSubscriptionFormService.save(requestForm);
        result.setIsSuccess(true);
        result.setRegisteredFormId(requestForm.getId());

        log.info("Form inserted");
        return result;
    }
}
