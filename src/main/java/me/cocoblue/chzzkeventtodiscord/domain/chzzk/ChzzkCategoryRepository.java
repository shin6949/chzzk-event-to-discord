package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChzzkCategoryRepository extends JpaRepository<ChzzkCategoryEntity, String> {
    Optional<ChzzkCategoryEntity> findByIdCategoryId(String categoryId);
}
