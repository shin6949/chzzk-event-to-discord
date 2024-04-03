package me.cocoblue.chzzkeventtodiscord.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChzzkVideoVo {
    @JsonProperty("videoNo")
    private Long videoNo;
    @JsonProperty("videoId")
    private String videoId;
    @JsonProperty("videoTitle")
    private String videoTitle;
    @JsonProperty("videoType")
    private String videoType;
    @JsonProperty("publishDate")
    private LocalDateTime publishDate;
    @JsonProperty("trailerUrl")
    private String trailerUrl;
    @JsonProperty("duration")
    private Long duration;
    @JsonProperty("readCount")
    private Long readCount;
    @JsonProperty("publishDateAt")
    private Long publishDateAt;
    @JsonProperty("categoryType")
    private String categoryType;
    @JsonProperty("videoCategory")
    private String videoCategory;
    @JsonProperty("videoCategoryValue")
    private String videoCategoryValue;
    @JsonProperty("exposure")
    private boolean exposure;
    @JsonProperty("channel")
    private ChzzkChannelVo channel;
}
