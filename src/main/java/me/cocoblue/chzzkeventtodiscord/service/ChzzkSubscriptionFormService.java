package me.cocoblue.chzzkeventtodiscord.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkSubscriptionType;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormRepository;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class ChzzkSubscriptionFormService {
    private final ChzzkSubscriptionFormRepository chzzkSubscriptionFormRepository;

    public List<ChzzkSubscriptionFormEntity> findAllByChannelEntityAndSubscriptionTypeAndEnabled(final String channelId, final ChzzkSubscriptionType type, final boolean enabled) {
        log.debug("Get subscription form by channelId: {} / type: {} / enabled: {}", channelId, type, enabled);
        final ChzzkChannelEntity chzzkChannelEntity = ChzzkChannelEntity.builder()
                .channelId(channelId)
                .build();

        return chzzkSubscriptionFormRepository.findAllByChzzkChannelEntityAndChzzkSubscriptionTypeAndEnabled(chzzkChannelEntity, type, enabled);
    }

    public List<ChzzkSubscriptionFormEntity> findAllByChannelEntityAndEnabled(final String channelId, final boolean enabled) {
        log.debug("Get subscription form by channelId: {} / enabled: {}", channelId, enabled);
        final ChzzkChannelEntity chzzkChannelEntity = ChzzkChannelEntity.builder()
                .channelId(channelId)
                .build();

        return chzzkSubscriptionFormRepository.findAllByChzzkChannelEntityAndEnabled(chzzkChannelEntity, enabled);
    }

    public List<ChzzkSubscriptionFormEntity> findAllByEnabled(final boolean enabled) {
        log.debug("Get subscription form by enabled: {}", enabled);
        return chzzkSubscriptionFormRepository.findAllByEnabled(enabled);
    }

    public void save(final ChzzkSubscriptionFormEntity chzzkSubscriptionFormEntity) {
        log.debug("Save subscription form: {}", chzzkSubscriptionFormEntity);
        chzzkSubscriptionFormRepository.saveAndFlush(chzzkSubscriptionFormEntity);
    }
}
