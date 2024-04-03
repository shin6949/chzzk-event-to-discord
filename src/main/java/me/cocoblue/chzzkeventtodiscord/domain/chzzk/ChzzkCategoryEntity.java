package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Column(name = "poster_image_url", length = 30000)
    private String posterImageUrl;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private ZonedDateTime updatedAt;
}
