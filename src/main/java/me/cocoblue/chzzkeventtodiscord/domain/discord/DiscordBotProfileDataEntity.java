package me.cocoblue.chzzkeventtodiscord.domain.discord;

import jakarta.persistence.*;
import lombok.*;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "bot_profile_data")
public class DiscordBotProfileDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name="owner_id", foreignKey = @ForeignKey(name="FK_BOT_PROFILE_DATA_OWNER_ID"), nullable = false)
    private ChzzkChannelEntity ownerId;

    @Column(length = 100, nullable = false)
    private String username;

    @Column(length = 600, nullable = false)
    private String avatarUrl;
}
