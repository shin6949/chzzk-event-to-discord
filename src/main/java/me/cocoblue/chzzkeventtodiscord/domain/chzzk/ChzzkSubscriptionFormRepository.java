package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkSubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChzzkSubscriptionFormRepository extends JpaRepository<ChzzkSubscriptionFormEntity, Long> {
    List<ChzzkSubscriptionFormEntity> findAllByChzzkChannelEntityAndChzzkSubscriptionTypeAndEnabled(ChzzkChannelEntity chzzkChannelEntity,
                                                                                                    ChzzkSubscriptionType chzzkSubscriptionType, boolean enabled);

    List<ChzzkSubscriptionFormEntity> findAllByChzzkChannelEntityAndEnabled(ChzzkChannelEntity chzzkChannelEntity, boolean enabled);

    List<ChzzkSubscriptionFormEntity> findAllByEnabled(boolean enabled);
}
