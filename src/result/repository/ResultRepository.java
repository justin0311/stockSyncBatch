package result.repository;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 실적 데이터를 가져오기 위한 JPA 인터페이스
 */
public interface ResultRepository extends JpaRepository<Result, Long>, ResultRepositoryCustom {
}
