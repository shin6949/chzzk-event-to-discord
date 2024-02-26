package me.cocoblue.chzzkeventtodiscord.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkCategoryResponseVO {
    @JsonProperty("code")
    private Long code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("content")
    private ChzzkCategoryContent content;
}

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ChzzkCategoryContent extends ChzzkCategoryCommonVO {
    @JsonProperty("posterImageUrl")
    private String posterImageUrl;
    @JsonProperty("openLiveCount")
    private Long openLiveCount;
    @JsonProperty("concurrentUserCount")
    private Long concurrentUserCount;
    @JsonProperty("tags")
    private Set<String> tags;
    @JsonProperty("existLounge")
    private boolean existLounge;
}
