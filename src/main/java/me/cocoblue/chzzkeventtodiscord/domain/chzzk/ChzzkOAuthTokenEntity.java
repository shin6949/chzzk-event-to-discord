package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "chzzk_oauth_token")
public class ChzzkOAuthTokenEntity {
    @Id
    @Column(name = "channel_id", nullable = false, length = 100)
    private String channelId;

    @Column(name = "access_token", nullable = false, length = 3000)
    private String accessToken;

    @Column(name = "refresh_token", length = 3000)
    private String refreshToken;

    @Column(name = "token_type", length = 30)
    private String tokenType;

    @Column(name = "scope", length = 1000)
    private String scope;

    @Column(name = "access_token_expires_at")
    private ZonedDateTime accessTokenExpiresAt;

    @Column(name = "refresh_token_expires_at")
    private ZonedDateTime refreshTokenExpiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
