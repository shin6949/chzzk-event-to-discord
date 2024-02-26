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
    private Long id;

    @Column(length = 500, nullable = false)
    private String name;

    @Column(length = 500, nullable = false)
    private String webhookUrl;

    @Column(columnDefinition = "TEXT")
    private String meno;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="owner_id", foreignKey = @ForeignKey(name="FK_WEBHOOK_DATA_OWNER_ID"), nullable = false)
    private ChzzkChannelEntity ownerId;
}
