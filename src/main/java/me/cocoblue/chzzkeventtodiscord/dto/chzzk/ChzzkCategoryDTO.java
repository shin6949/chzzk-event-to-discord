package me.cocoblue.chzzkeventtodiscord.dto.chzzk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.data.ChzzkCategoryType;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkCategoryEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkCategoryDTO {
    private ChzzkCategoryType categoryType;
    private String categoryId;
    private String categoryValue;

    public ChzzkCategoryDTO(ChzzkCategoryEntity entity) {
        this.categoryType = entity.getCategoryType();
        this.categoryId = entity.getCategoryId();
        this.categoryValue = entity.getCategoryName();
    }
}
