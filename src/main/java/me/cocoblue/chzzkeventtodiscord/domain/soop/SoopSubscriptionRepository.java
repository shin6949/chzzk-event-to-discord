package me.cocoblue.chzzkeventtodiscord.domain.soop;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SoopSubscriptionRepository extends JpaRepository<SoopSubscriptionEntity, Long> {
    List<SoopSubscriptionEntity> findAllByEnabled(boolean enabled);
    Page<SoopSubscriptionEntity> findAllByFormOwner_ChannelId(String channelId, Pageable pageable);
    boolean existsByWebhook_Id(Long webhookId);
    boolean existsByBotProfile_Id(Long botProfileId);
}
