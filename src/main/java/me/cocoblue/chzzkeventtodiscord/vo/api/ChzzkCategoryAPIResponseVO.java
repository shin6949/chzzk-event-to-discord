package me.cocoblue.chzzkeventtodiscord.vo.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkCategoryEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkCategoryId;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkCategoryDTO;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChzzkCategoryAPIResponseVO {
    @JsonProperty("code")
    private int code;
    @JsonProperty("message")
    private String message;
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
