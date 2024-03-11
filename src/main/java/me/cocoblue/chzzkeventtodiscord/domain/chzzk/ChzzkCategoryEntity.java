package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

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

    @Column(name = "poster_image_url", length = 32780)
    private String posterImageUrl;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;
}
