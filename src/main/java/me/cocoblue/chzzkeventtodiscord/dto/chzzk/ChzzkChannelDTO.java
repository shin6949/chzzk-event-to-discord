package me.cocoblue.chzzkeventtodiscord.dto.chzzk;

import lombok.*;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChzzkChannelDTO {
    private String channelId;
    private String channelName;
    private String channelImageUrl;
    private Boolean verifiedMark;
    private String channelDescription;
    private int followerCount;
    private boolean openLive;
    private boolean subscriptionAvailability;

    public ChzzkChannelDTO(ChzzkChannelEntity entity) {
        this.channelId = entity.getChannelId();
        this.channelName = entity.getChannelName();
        this.channelImageUrl = entity.getProfileUrl();
        this.verifiedMark = entity.isVerifiedMark();
        this.channelDescription = entity.getChannelDescription();
        this.followerCount = entity.getFollowerCount();
        this.subscriptionAvailability = entity.isSubscriptionAvailability();
        this.openLive = entity.isLive();
    }

    public ChzzkChannelEntity toEntity() {
        return ChzzkChannelEntity.builder()
                .channelId(channelId)
                .channelName(channelName)
                .isVerifiedMark(verifiedMark)
                .profileUrl(channelImageUrl)
                .channelDescription(channelDescription)
                .followerCount(followerCount)
                .subscriptionAvailability(subscriptionAvailability)
                .isLive(openLive)
                .build();
    }
}
