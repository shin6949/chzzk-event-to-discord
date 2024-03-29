package me.cocoblue.chzzkeventtodiscord.vo.api;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkChannelVO;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkLiveVO;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkVideoVO;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChzzkSearchAPIResponseVO extends ChzzkAPICommonResponseVO {
    @JsonProperty("content")
    private ChzzkContentVO content;

    public int getContentSize() {
        return content.getSize();
    }

    public ChzzkChannelVO getChannel(int index) {
        return content.getData().get(index).getChannel();
    }

    public ChzzkLiveVO getLive(int index) {
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
class ChzzkContentVO {
    @JsonProperty("size")
    private int size;
    @JsonProperty("page")
    private Map<String, Object> page;
    @JsonProperty("data")
    private List<ChzzkAPIResponseData> data;
}

@Getter
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
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class ChzzkContentDetailVO {
    @JsonProperty("live")
    private ChzzkLiveVO live;
    @JsonProperty("videos")
    private List<ChzzkVideoVO> videos;
}