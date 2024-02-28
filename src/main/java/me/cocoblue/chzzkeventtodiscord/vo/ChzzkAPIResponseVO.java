package me.cocoblue.chzzkeventtodiscord.vo;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkAPIResponseVO {
    @JsonProperty("code")
    private int code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("content")
    private ChzzkContentVO content;
}

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ChzzkContentVO {
    @JsonProperty("size")
    private int size;
    @JsonProperty("page")
    private Map<String, Object> page;
    @JsonProperty("data")
    private List<ChzzkAPIResponseData> data;
}

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ChzzkAPIResponseData {
    @JsonProperty("channel")
    private ChzzkChannelVO channel;
    @JsonProperty("content")
    private ChzzkContentDetailVO content;
}

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ChzzkContentDetailVO {
    @JsonProperty("live")
    private ChzzkLiveVO live;
    @JsonProperty("videos")
    private List<ChzzkVideoVO> videos;
}