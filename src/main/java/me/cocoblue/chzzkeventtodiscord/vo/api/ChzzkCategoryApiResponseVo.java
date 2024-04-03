package me.cocoblue.chzzkeventtodiscord.vo.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkCategoryDto;

@Data
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChzzkCategoryApiResponseVo {
    @JsonProperty("code")
    private int code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("content")
    private ChzzkCategoryContent content;

    public ChzzkCategoryDto toDto() {
        return ChzzkCategoryDto.builder()
                .categoryType(content.getCategoryType())
                .categoryId(content.getCategoryId())
                .categoryValue(content.getCategoryValue())
                .posterImageUrl(content.getPosterImageUrl())
                .build();
    }
}

@Data
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ChzzkCategoryContent {
    @JsonProperty("categoryType")
    private String categoryType;
    @JsonProperty("categoryId")
    private String categoryId;
    @JsonProperty("categoryValue")
    private String categoryValue;
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
