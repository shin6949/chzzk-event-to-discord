package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.data.ChzzkCategoryType;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Builder
public class ChzzkCategoryId implements Serializable {
    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type")
    private ChzzkCategoryType categoryType;
}
