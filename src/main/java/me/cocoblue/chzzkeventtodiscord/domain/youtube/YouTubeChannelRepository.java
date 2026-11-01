package me.cocoblue.chzzkeventtodiscord.domain.youtube;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YouTubeChannelRepository extends JpaRepository<YouTubeChannelEntity, String> {
}
