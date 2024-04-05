package me.cocoblue.chzzkeventtodiscord.domain.discord;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "discord_bot_profile_data")
public class DiscordBotProfileDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "owner_id", foreignKey = @ForeignKey(name = "FK_BOT_PROFILE_DATA_OWNER_ID"), nullable = false)
    private ChzzkChannelEntity ownerId;

    @Column(name = "username", length = 100, nullable = false)
    private String username;

    @Column(name = "avatar_url", length = 30000, nullable = false)
    private String avatarUrl;
}
