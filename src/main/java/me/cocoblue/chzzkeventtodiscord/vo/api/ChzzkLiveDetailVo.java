package me.cocoblue.chzzkeventtodiscord.vo.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableConditionType;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableGroupType;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkLiveDetailDto;

import java.time.LocalDateTime;

// For Chzzk Live Detail API
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChzzkLiveDetailVo extends BaseChzzkLiveVo {
    @JsonProperty("closeDate")
    private LocalDateTime closeDate;
    @JsonProperty("adult")
    private boolean adult;
    @JsonProperty("chatActive")
    private boolean chatActive;
    @JsonProperty("chatAvailableGroup")
    private ChzzkChatAvailableGroupType chatAvailableGroup;
    @JsonProperty("chatAvailableCondition")
    private ChzzkChatAvailableConditionType chatAvailableCondition;
    @JsonProperty("minFollowerMinute")
    private int minFollowerMinute;

    public ChzzkLiveDetailDto toDto() {
        return ChzzkLiveDetailDto.builder()
                .liveId(liveId)
                .liveTitle(liveTitle)
                .liveImageUrl(liveImageUrl)
                .defaultThumbnailImageUrl(defaultThumbnailImageUrl)
                .concurrentUserCount(concurrentUserCount)
                .accumulateCount(accumulateCount)
                .openDate(openDate)
                .closeDate(closeDate)
                .chatChannelId(chatChannelId)
                .categoryType(categoryType)
                .categoryId(liveCategory)
                .categoryValue(liveCategoryValue)
                .adult(adult)
                .chatActive(chatActive)
                .chatAvailableGroup(chatAvailableGroup)
                .chatAvailableCondition(chatAvailableCondition)
                .minFollowerMinute(minFollowerMinute)
                .build();
    }
}
