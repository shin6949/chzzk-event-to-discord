package me.cocoblue.chzzkeventtodiscord.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkChannelVO {
    @JsonProperty("channelId")
    private String channelId;
    @JsonProperty("channelName")
    private String channelName;
    @JsonProperty("channelImageUrl")
    private String channelImageUrl;
    @JsonProperty("verifiedMark")
    private boolean verifiedMark;
    @JsonProperty("channelDescription")
    private String channelDescription;
    @JsonProperty("followerCount")
    private int followerCount;
    @JsonProperty("openLive")
    private boolean openLive;
    @JsonProperty("personalData")
    private ChzzkPersonalDataVO personalData;
}

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ChzzkPersonalDataVO {
    @JsonProperty("following")
    private ChzzkFollowingVO following;
    @JsonProperty("privateUserBlock")
    private boolean privateUserBlock;
}

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ChzzkFollowingVO {
    @JsonProperty("following")
    private boolean following;
    @JsonProperty("notification")
    private boolean notification;
    @JsonProperty("followDate")
    private LocalDate followDate;
}
