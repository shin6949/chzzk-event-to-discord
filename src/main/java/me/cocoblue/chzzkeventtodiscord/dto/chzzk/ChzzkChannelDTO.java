package me.cocoblue.chzzkeventtodiscord.dto.chzzk;

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
}
