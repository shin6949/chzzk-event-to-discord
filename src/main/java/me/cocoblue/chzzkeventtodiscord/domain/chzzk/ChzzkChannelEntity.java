package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

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
    @Column(length = 300, name = "nickname", nullable = false)
    private String nickname;
    @Column(length = 500, name = "profile_url")
    private String profileUrl;
    @Column(name = "is_live", nullable = false)
    private boolean isLive;
    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;

}
