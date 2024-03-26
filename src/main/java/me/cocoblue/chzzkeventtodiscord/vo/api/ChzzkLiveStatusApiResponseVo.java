package me.cocoblue.chzzkeventtodiscord.vo.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableConditionType;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableGroupType;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkLiveStatusType;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkLiveStatusDto;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChzzkLiveStatusApiResponseVo extends ChzzkApiCommonResponseVo {
    @JsonProperty("content")
    private ChzzkLiveStatusVo content;

    public ChzzkLiveStatusDto toDto() {
        return ChzzkLiveStatusDto.builder()
                .liveTitle(content.getLiveTitle())
                .status(content.getStatus())
                .concurrentUserCount(content.getConcurrentUserCount())
                .accumulateCount(content.getAccumulateCount())
                .paidPromotion(content.isPaidPromotion())
                .adult(content.isAdult())
                .chatChannelId(content.getChatChannelId())
                .chatActive(content.isChatActive())
                .chatAvailableGroup(content.getChatAvailableGroup())
                .chatAvailableCondition(content.getChatAvailableCondition())
                .minFollowerMinute(content.getMinFollowerMinute())
                .categoryType(content.getCategoryType())
                .categoryId(content.getCategoryId())
                .categoryValue(content.getCategoryValue())
                .chatDonationRankingExposure(content.isChatDonationRankingExposure())
                .build();
    }
}

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class ChzzkLiveStatusVo {
    @JsonProperty("liveTitle")
    private String liveTitle;
    @JsonProperty("status")
    private ChzzkLiveStatusType status;
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
    private ChzzkLiveStatusType faultStatus;
    @JsonProperty("userAdultStatus")
    private String userAdultStatus;
    @JsonProperty("chatActive")
    private boolean chatActive;
    @JsonProperty("chatAvailableGroup")
    private ChzzkChatAvailableGroupType chatAvailableGroup;
    @JsonProperty("chatAvailableCondition")
    private ChzzkChatAvailableConditionType chatAvailableCondition;
    @JsonProperty("minFollowerMinute")
    private Long minFollowerMinute;
    @JsonProperty("categoryType")
    private String categoryType;
    @JsonProperty("liveCategory")
    private String categoryId;
    @JsonProperty("liveCategoryValue")
    private String categoryValue;
    /* @since Chzzk 24.03.21. Update */
    @JsonProperty("chatDonationRankingExposure")
    private boolean chatDonationRankingExposure;
}