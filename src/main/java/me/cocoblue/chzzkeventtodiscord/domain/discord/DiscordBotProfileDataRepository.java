package me.cocoblue.chzzkeventtodiscord.domain.discord;

import java.util.Optional;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscordBotProfileDataRepository extends JpaRepository<DiscordBotProfileDataEntity, Long> {
    Optional<DiscordBotProfileDataEntity> findDiscordBotProfileDataEntityByAvatarUrlAndOwnerIdAndUsername(String avatarUrl,
                                                                                                          ChzzkChannelEntity ownerId,
                                                                                                          String username);
}
