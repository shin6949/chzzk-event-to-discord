package me.cocoblue.chzzkeventtodiscord.dto.chzzk;

import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkChannelDTO {
    private String channelId;
    private String channelName;
    private String channelImageUrl;
    private boolean verifiedMark;
    private String channelDescription;
    private int followerCount;
    private boolean openLive;

    public ChzzkChannelDTO(ChzzkChannelEntity entity) {
        this.channelId = entity.getChannelId();
        this.channelName = entity.getChannelName();
        this.channelImageUrl = entity.getProfileUrl();
        this.verifiedMark = entity.isVerifiedMark();
        this.channelDescription = entity.getChannelDescription();
        this.followerCount = entity.getFollowerCount();
        this.openLive = entity.isLive();
    }
}
