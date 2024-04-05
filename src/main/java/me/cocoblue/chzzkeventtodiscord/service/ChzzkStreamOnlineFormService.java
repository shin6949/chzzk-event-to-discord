package me.cocoblue.chzzkeventtodiscord.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkSubscriptionType;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkStreamOnlineFormEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkStreamOnlineFormRepository;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class ChzzkStreamOnlineFormService {
    private final ChzzkStreamOnlineFormRepository chzzkStreamOnlineFormRepository;

    public List<ChzzkStreamOnlineFormEntity> findAllByChannelEntityAndSubscriptionTypeAndEnabled(final String channelId, final ChzzkSubscriptionType type, final boolean enabled) {
        log.debug("Get subscription form by channelId: {} / type: {} / enabled: {}", channelId, type, enabled);
        final ChzzkChannelEntity chzzkChannelEntity = ChzzkChannelEntity.builder()
                .channelId(channelId)
                .build();

        return chzzkStreamOnlineFormRepository.findAllByChzzkChannelEntityAndChzzkSubscriptionTypeAndEnabled(chzzkChannelEntity, type, enabled);
    }

    public List<ChzzkStreamOnlineFormEntity> findAllByChannelEntityAndEnabled(final String channelId, final boolean enabled) {
        log.debug("Get subscription form by channelId: {} / enabled: {}", channelId, enabled);
        final ChzzkChannelEntity chzzkChannelEntity = ChzzkChannelEntity.builder()
                .channelId(channelId)
                .build();

        return chzzkStreamOnlineFormRepository.findAllByChzzkChannelEntityAndEnabled(chzzkChannelEntity, enabled);
    }

    public List<ChzzkStreamOnlineFormEntity> findAllByEnabled(final boolean enabled) {
        log.debug("Get subscription form by enabled: {}", enabled);
        return chzzkStreamOnlineFormRepository.findAllByEnabled(enabled);
    }

    public void save(final ChzzkStreamOnlineFormEntity chzzkStreamOnlineFormEntity) {
        log.debug("Save subscription form: {}", chzzkStreamOnlineFormEntity);
        chzzkStreamOnlineFormRepository.saveAndFlush(chzzkStreamOnlineFormEntity);
    }
}
