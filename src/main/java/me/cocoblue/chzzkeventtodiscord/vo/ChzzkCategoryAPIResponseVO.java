package me.cocoblue.chzzkeventtodiscord.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.data.ChzzkCategoryType;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkCategoryEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkCategoryId;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkCategoryDTO;

import java.time.ZonedDateTime;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkCategoryAPIResponseVO {
    @JsonProperty("code")
    private Long code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("content")
    private ChzzkCategoryContent content;

    public ChzzkCategoryEntity toEntity() {
        return ChzzkCategoryEntity.builder()
                .id(ChzzkCategoryId.builder()
                        .categoryId(content.getCategoryId())
                        .categoryType(ChzzkCategoryType.valueOf(content.getCategoryType()))
                        .build())
                .categoryName(content.getCategoryValue())
                .posterImageUrl(content.getPosterImageUrl())
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    public ChzzkCategoryDTO toDTO() {
        return ChzzkCategoryDTO.builder()
                .categoryType(ChzzkCategoryType.valueOf(content.getCategoryType()))
                .categoryId(content.getCategoryId())
                .categoryValue(content.getCategoryValue())
                .build();
    }
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
