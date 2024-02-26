package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "chzzk_category")
@Builder
public class ChzzkCategoryEntity {
    @Id
    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(name = "category_type", nullable = false)
    private String categoryType;

    @Column(name = "poster_image_url", nullable = false)
    private String posterImageUrl;
}
