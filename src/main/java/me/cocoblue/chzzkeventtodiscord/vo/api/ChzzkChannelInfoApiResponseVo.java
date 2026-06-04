package me.cocoblue.chzzkeventtodiscord.vo.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkChannelVo;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChzzkChannelInfoApiResponseVo extends ChzzkApiCommonResponseVo {
    @JsonProperty("content")
    private ChzzkChannelInfoContentVo content;

    public ChzzkChannelVo getFirstChannel() {
        if (content == null || content.data == null || content.data.isEmpty()) {
            return null;
        }
        return content.data.get(0);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChzzkChannelInfoContentVo {
        @JsonProperty("data")
        private List<ChzzkChannelVo> data;
    }
}
