package me.cocoblue.chzzkeventtodiscord.vo.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableConditionType;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableGroupType;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkLiveDetailDto;

import java.time.LocalDateTime;

// For Chzzk Live Detail API
@Getter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChzzkLiveDetailVo extends ChzzkApiCommonResponseVo {
    @JsonProperty("content")
    private ChzzkLiveDetailVoContent content;

    public ChzzkLiveDetailDto toDto() {
        return ChzzkLiveDetailDto.builder()
            .liveId(content.getLiveId())
            .liveTitle(content.getLiveTitle())
            .liveImageUrl(content.getLiveImageUrl())
            .defaultThumbnailImageUrl(content.getDefaultThumbnailImageUrl())
            .concurrentUserCount(content.getConcurrentUserCount())
            .accumulateCount(content.getAccumulateCount())
            .openDate(content.getOpenDate())
            .closeDate(content.getCloseDate())
            .chatChannelId(content.getChatChannelId())
            .categoryType(content.getCategoryType())
            .categoryId(content.getLiveCategory())
            .categoryValue(content.getLiveCategoryValue())
            .adult(content.isAdult())
            .chatActive(content.isChatActive())
            .chatAvailableGroup(content.getChatAvailableGroup())
            .chatAvailableCondition(content.getChatAvailableCondition())
            .minFollowerMinute(content.getMinFollowerMinute())
            .tags(content.getTags())
            .build();
    }
}

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
class ChzzkLiveDetailVoContent extends BaseChzzkLiveVo {
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
}
