package me.cocoblue.chzzkeventtodiscord.domain.discord;

import jakarta.persistence.*;
import lombok.*;
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

    @Column(name = "avatar_url", length = 600, nullable = false)
    private String avatarUrl;
}
