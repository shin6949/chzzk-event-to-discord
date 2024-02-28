package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.data.ChzzkCategoryType;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "chzzk_category")
@Builder
public class ChzzkCategoryEntity {
    @EmbeddedId
    private ChzzkCategoryId id;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(name = "poster_image_url", nullable = false)
    private String posterImageUrl;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
