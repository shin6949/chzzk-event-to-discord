package me.cocoblue.chzzkeventtodiscord.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkLiveDto;
import me.cocoblue.chzzkeventtodiscord.vo.api.BaseChzzkLiveVo;

// For Chzzk Search API
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChzzkLiveVo extends BaseChzzkLiveVo {
    @JsonProperty("channelId")
    private String channelId;

    public ChzzkLiveDto toDto() {
        return ChzzkLiveDto.builder()
                .liveTitle(liveTitle)
                .liveImageUrl(liveImageUrl)
                .defaultThumbnailImageUrl(defaultThumbnailImageUrl)
                .concurrentUserCount(concurrentUserCount)
                .accumulateCount(accumulateCount)
                .openDate(openDate)
                .liveId(liveId)
                .chatChannelId(chatChannelId)
                .categoryType(categoryType)
                .liveCategory(liveCategory)
                .liveCategoryValue(liveCategoryValue)
                .channelId(channelId)
                .build();
    }
}
