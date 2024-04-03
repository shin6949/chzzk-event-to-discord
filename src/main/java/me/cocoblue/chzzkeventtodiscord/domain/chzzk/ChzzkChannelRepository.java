package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChzzkChannelRepository extends JpaRepository<ChzzkChannelEntity, String> {
    Optional<ChzzkChannelEntity> findChzzkChannelEntityByChannelId(String channelId);

    Optional<ChzzkChannelEntity> findChzzkChannelEntityByChannelName(String channelName);
}
