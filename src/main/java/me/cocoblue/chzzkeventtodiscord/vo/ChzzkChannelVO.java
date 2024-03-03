package me.cocoblue.chzzkeventtodiscord.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkChannelDTO;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonProperty("subscriptionAvailability")
    private boolean subscriptionAvailability;
    @JsonProperty("subscriptionPaymentAvailability")
    private ChzzkSubscriptionPaymentAvailabilityVO subscriptionPaymentAvailability;

    public ChzzkChannelDTO toDTO() {
        return ChzzkChannelDTO.builder()
                .channelId(channelId)
                .channelName(channelName)
                .channelImageUrl(channelImageUrl)
                .verifiedMark(verifiedMark)
                .channelDescription(channelDescription)
                .followerCount(followerCount)
                .openLive(openLive)
                .verifiedMark(verifiedMark)
                .followerCount(followerCount)
                .channelDescription(channelDescription)
                .subscriptionAvailability(subscriptionAvailability)
                .build();
    }
}

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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
@JsonIgnoreProperties(ignoreUnknown = true)
class ChzzkFollowingVO {
    @JsonProperty("following")
    private boolean following;
    @JsonProperty("notification")
    private boolean notification;
    @JsonProperty("followDate")
    private LocalDate followDate;
}

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class ChzzkSubscriptionPaymentAvailabilityVO {
    @JsonProperty("iapAvailability")
    private boolean iapAvailability;
    @JsonProperty("iabAvailability")
    private boolean iabAvailability;
}

