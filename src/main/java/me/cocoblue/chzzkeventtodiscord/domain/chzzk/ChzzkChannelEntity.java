package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "chzzk_channel")
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ChzzkChannelEntity {
    @Id
    @Column(name = "channel_id", nullable = false)
    private String channelId;
    @Column(length = 300, name = "channel_name", nullable = false)
    private String channelName;
    @Column(length = 500, name = "profile_url")
    private String profileUrl;
    @Column(name = "is_live", nullable = false)
    private boolean isLive;
    @Column(name = "last_check_time", nullable = false)
    private ZonedDateTime lastCheckTime;
}
