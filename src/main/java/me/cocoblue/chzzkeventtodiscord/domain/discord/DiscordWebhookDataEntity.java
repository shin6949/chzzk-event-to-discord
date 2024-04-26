package me.cocoblue.chzzkeventtodiscord.domain.discord;

import jakarta.persistence.*;
import lombok.*;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "discord_webhook_data")
@Builder
public class DiscordWebhookDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", length = 500, nullable = false)
    private String name;

    @Column(name = "webhook_url", length = 30000, nullable = false)
    private String webhookUrl;

    @Column(name = "meno", length = 500)
    private String meno;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "owner_id", foreignKey = @ForeignKey(name = "fk_discord_webhook_data_owner_id"), nullable = false)
    private ChzzkChannelEntity ownerId;
}
