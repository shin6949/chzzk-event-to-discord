package me.cocoblue.chzzkeventtodiscord.vo.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkCategoryCommonVO;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkLiveInfoAPIResponseVO {
    @JsonProperty("code")
    private int code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("content")
    private ChzzkLiveInfoVO content;
}

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ChzzkLiveInfoVO extends ChzzkCategoryCommonVO {
    @JsonProperty("liveTitle")
    private String liveTitle;
    @JsonProperty("status")
    private String status;
    @JsonProperty("concurrentUserCount")
    private Long concurrentUserCount;
    @JsonProperty("accumulateCount")
    private Long accumulateCount;
    @JsonProperty("paidPromotion")
    private boolean paidPromotion;
    @JsonProperty("adult")
    private boolean adult;
    @JsonProperty("chatChannelId")
    private String chatChannelId;
    @JsonProperty("livePollingStatusJson")
    private String livePollingStatusJson;
    @JsonProperty("faultStatus")
    private String faultStatus;
    @JsonProperty("userAdultStatus")
    private String userAdultStatus;
    @JsonProperty("chatActive")
    private boolean chatActive;
    @JsonProperty("chatAvailableGroup")
    private String chatAvailableGroup;
    @JsonProperty("chatAvailableCondition")
    private String chatAvailableCondition;
    @JsonProperty("minFollowerMinute")
    private Long minFollowerMinute;
}