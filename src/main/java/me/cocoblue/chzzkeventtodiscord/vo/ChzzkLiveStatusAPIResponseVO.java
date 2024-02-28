package me.cocoblue.chzzkeventtodiscord.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkLiveStatusDTO;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkLiveStatusAPIResponseVO {
    @JsonProperty("code")
    private int code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("content")
    private ChzzkLiveStatusVO content;

    public ChzzkLiveStatusDTO toDTO() {
        return ChzzkLiveStatusDTO.builder()
                .liveTitle(content.getLiveTitle())
                .status(content.getStatus())
                .concurrentUserCount(content.getConcurrentUserCount())
                .accumulateCount(content.getAccumulateCount())
                .paidPromotion(content.isPaidPromotion())
                .adult(content.isAdult())
                .chatChannelId(content.getChatChannelId())
                .livePollingStatusJson(content.getLivePollingStatusJson())
                .faultStatus(content.getFaultStatus())
                .userAdultStatus(content.getUserAdultStatus())
                .chatActive(content.isChatActive())
                .chatAvailableGroup(content.getChatAvailableGroup())
                .chatAvailableCondition(content.getChatAvailableCondition())
                .minFollowerMinute(content.getMinFollowerMinute())
                .build();
    }
}

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ChzzkLiveStatusVO extends ChzzkCategoryCommonVO {
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