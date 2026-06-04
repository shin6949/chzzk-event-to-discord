package me.cocoblue.chzzkeventtodiscord.domain.discord;

import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscordBotProfileDataRepository extends JpaRepository<DiscordBotProfileDataEntity, Long> {
    Optional<DiscordBotProfileDataEntity> findDiscordBotProfileDataEntityByAvatarUrlAndOwnerIdAndUsername(String avatarUrl,
                                                                                                          ChzzkChannelEntity ownerId,
                                                                                                          String username);

    Page<DiscordBotProfileDataEntity> findAllByOwnerId_ChannelId(String channelId, Pageable pageable);

    Optional<DiscordBotProfileDataEntity> findByIdAndOwnerId_ChannelId(Long id, String channelId);

    boolean existsByAliasAndOwnerId_ChannelId(String alias, String channelId);
}
