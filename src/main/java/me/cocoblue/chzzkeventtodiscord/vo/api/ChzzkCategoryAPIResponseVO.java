package me.cocoblue.chzzkeventtodiscord.vo.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkCategoryEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkCategoryId;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkCategoryDTO;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkCategoryCommonVO;

import java.time.ZonedDateTime;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChzzkCategoryAPIResponseVO extends ChzzkAPICommonResponseVO {
    @JsonProperty("content")
    private ChzzkCategoryContent content;

    public ChzzkCategoryEntity toEntity() {
        return ChzzkCategoryEntity.builder()
                .id(ChzzkCategoryId.builder()
                        .categoryId(content.getCategoryId())
                        .categoryType(content.getCategoryType())
                        .build())
                .categoryName(content.getCategoryValue())
                .posterImageUrl(content.getPosterImageUrl())
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    public ChzzkCategoryDTO toDTO() {
        return ChzzkCategoryDTO.builder()
                .categoryType(content.getCategoryType())
                .categoryId(content.getCategoryId())
                .categoryValue(content.getCategoryValue())
                .posterImageUrl(content.getPosterImageUrl())
                .build();
    }
}

@Getter
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
