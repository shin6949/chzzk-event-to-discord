package me.cocoblue.chzzkeventtodiscord.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// For Chzzk Search API
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChzzkLiveVO {
    @JsonProperty("liveTitle")
    private String liveTitle;
    @JsonProperty("liveImageUrl")
    private String liveImageUrl;
    @JsonProperty("defaultThumbnailImageUrl")
    private String defaultThumbnailImageUrl;
    @JsonProperty("concurrentUserCount")
    private int concurrentUserCount;
    @JsonProperty("accumulateCount")
    private int accumulateCount;
    @JsonProperty("openDate")
    private LocalDateTime openDate;
    @JsonProperty("liveId")
    private String liveId;
    @JsonProperty("chatChannelId")
    private String chatChannelId;
    @JsonProperty("categoryType")
    private String categoryType;
    @JsonProperty("liveCategory")
    private String liveCategory;
    @JsonProperty("liveCategoryValue")
    private String liveCategoryValue;
    @JsonProperty("channelId")
    private String channelId;
    @JsonProperty("livePlaybackJson")
    private String livePlaybackJson;
}
