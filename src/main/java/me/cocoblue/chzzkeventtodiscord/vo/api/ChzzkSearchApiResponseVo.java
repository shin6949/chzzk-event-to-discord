package me.cocoblue.chzzkeventtodiscord.vo.api;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkChannelVo;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkLiveVo;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkVideoVo;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChzzkSearchApiResponseVo extends ChzzkApiCommonResponseVo {
    @JsonProperty("content")
    private ChzzkContentVo content;

    public int getContentSize() {
        return content.getSize();
    }

    public ChzzkChannelVo getChannel(int index) {
        return content.getData().get(index).getChannel();
    }

    public ChzzkLiveVo getLive(int index) {
        if (content.getData().get(index).getContent() == null) {
            return null;
        }
        return content.getData().get(index).getContent().getLive();
    }
}

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class ChzzkContentVo {
    @JsonProperty("size")
    private int size;
    @JsonProperty("page")
    private Map<String, Object> page;
    @JsonProperty("data")
    private List<ChzzkApiResponseData> data;
}

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class ChzzkApiResponseData {
    @JsonProperty("channel")
    private ChzzkChannelVo channel;
    @JsonProperty("content")
    private ChzzkContentDetailVo content;
}

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class ChzzkContentDetailVo {
    @JsonProperty("live")
    private ChzzkLiveVo live;
    @JsonProperty("videos")
    private List<ChzzkVideoVo> videos;
}