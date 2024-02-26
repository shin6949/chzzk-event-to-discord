package me.cocoblue.chzzkeventtodiscord.domain.discord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscordBotProfileDataRepository extends JpaRepository<DiscordBotProfileDataEntity, Long> {
}
