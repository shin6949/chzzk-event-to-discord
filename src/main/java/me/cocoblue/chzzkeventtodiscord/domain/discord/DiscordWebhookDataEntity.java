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

    @Column(name = "webhoook_url", length = 500, nullable = false)
    private String webhookUrl;

    @Column(name = "meno")
    private String meno;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "owner_id", foreignKey = @ForeignKey(name = "FK_WEBHOOK_DATA_OWNER_ID"), nullable = false)
    private ChzzkChannelEntity ownerId;
}
