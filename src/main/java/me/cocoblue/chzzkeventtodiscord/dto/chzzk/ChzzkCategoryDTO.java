package me.cocoblue.chzzkeventtodiscord.dto.chzzk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkCategoryEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkCategoryId;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkCategoryDTO {
    private String categoryType;
    private String categoryId;
    private String categoryValue;
    private String posterImageUrl;

    public ChzzkCategoryDTO(ChzzkCategoryEntity entity) {
        this.categoryType = entity.getId().getCategoryType();
        this.categoryId = entity.getId().getCategoryId();
        this.categoryValue = entity.getCategoryName();
        this.posterImageUrl = entity.getPosterImageUrl();
    }

    public ChzzkCategoryEntity toEntity() {
        return ChzzkCategoryEntity.builder()
                .id(ChzzkCategoryId.builder()
                        .categoryId(categoryId)
                        .categoryType(categoryType)
                        .build())
                .categoryName(categoryValue)
                .posterImageUrl(posterImageUrl)
                .updatedAt(ZonedDateTime.now())
                .build();
    }
}
