package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChzzkChannelRepository extends JpaRepository<ChzzkChannelEntity, String> {
    Optional<ChzzkChannelEntity> findByChannelId(String channelId);
    Optional<ChzzkChannelEntity> findByChannelName(String channelName);
}
