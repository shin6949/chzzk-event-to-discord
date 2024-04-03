package me.cocoblue.chzzkeventtodiscord.domain.discord;

import jakarta.persistence.CascadeType;
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
