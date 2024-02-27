package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChzzkCategoryRepository extends JpaRepository<ChzzkCategoryEntity, String> {
    Optional<ChzzkCategoryEntity> findByCategoryId(String categoryId);
}
