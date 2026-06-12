package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * {@code ChzzkCategoryRepository}는 엔티티의 데이터 접근과 조회 메서드를 제공합니다.
 *
 * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
 *
 * @since Ver.0.1
 */
@Repository
public interface ChzzkCategoryRepository extends JpaRepository<ChzzkCategoryEntity, String> {
  /**
   * {@code findByIdCategoryId}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-03-04 02:28:28 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 db09ddc.
   *
   * @since Ver.0.1
   */
  Optional<ChzzkCategoryEntity> findByIdCategoryId(String categoryId);
}
