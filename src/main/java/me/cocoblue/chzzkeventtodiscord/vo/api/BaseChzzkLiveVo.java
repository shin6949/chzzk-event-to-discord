package me.cocoblue.chzzkeventtodiscord.vo.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseChzzkLiveVo {
    @JsonProperty("liveId")
    protected String liveId;
    @JsonProperty("liveTitle")
    protected String liveTitle;
    @JsonProperty("liveImageUrl")
    protected String liveImageUrl;
    @JsonProperty("defaultThumbnailImageUrl")
    protected String defaultThumbnailImageUrl;
    @JsonProperty("concurrentUserCount")
    protected int concurrentUserCount;
    @JsonProperty("accumulateCount")
    protected int accumulateCount;
    @JsonProperty("openDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime openDate;
    @JsonProperty("chatChannelId")
    protected String chatChannelId;
    @JsonProperty("categoryType")
    protected String categoryType;
    @JsonProperty("liveCategory")
    protected String liveCategory;
    @JsonProperty("liveCategoryValue")
    protected String liveCategoryValue;
    @JsonProperty("livePlaybackJson")
    protected String livePlaybackJson;
}