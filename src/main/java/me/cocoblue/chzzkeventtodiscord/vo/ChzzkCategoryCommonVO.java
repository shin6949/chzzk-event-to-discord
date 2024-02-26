package me.cocoblue.chzzkeventtodiscord.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkCategoryCommonVO {
    @JsonProperty("categoryType")
    private String categoryType;
    @JsonProperty("categoryId")
    private String categoryId;
    @JsonProperty("categoryValue")
    private String categoryValue;
}
