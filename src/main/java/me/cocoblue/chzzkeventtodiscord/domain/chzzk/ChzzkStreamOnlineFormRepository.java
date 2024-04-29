package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkSubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChzzkStreamOnlineFormRepository extends JpaRepository<ChzzkStreamOnlineFormEntity, ChzzkSubscriptionFormEntity> {
    List<ChzzkStreamOnlineFormEntity> findAllByChzzkChannelEntityAndChzzkSubscriptionTypeAndEnabled(ChzzkChannelEntity chzzkChannelEntity,
                                                                                                    ChzzkSubscriptionType chzzkSubscriptionType, boolean enabled);

    List<ChzzkStreamOnlineFormEntity> findAllByChzzkChannelEntityAndEnabled(ChzzkChannelEntity chzzkChannelEntity, boolean enabled);

    List<ChzzkStreamOnlineFormEntity> findAllByEnabled(boolean enabled);
}
