package me.cocoblue.chzzkeventtodiscord.vo.api;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkChannelVO;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkLiveVO;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkVideoVO;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChzzkSearchAPIResponseVO extends ChzzkAPICommonResponseVO {
    @JsonProperty("content")
    private ChzzkContentVO content;
}

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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
@JsonIgnoreProperties(ignoreUnknown = true)
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
@JsonIgnoreProperties(ignoreUnknown = true)
class ChzzkContentDetailVO {
    @JsonProperty("live")
    private ChzzkLiveVO live;
    @JsonProperty("videos")
    private List<ChzzkVideoVO> videos;
}