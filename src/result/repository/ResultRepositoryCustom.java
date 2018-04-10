package result.repository;

import java.util.Date;
import java.util.List;

/**
 * 실적 데이터 가져오기 위한 JPA 커스텀 인터페이스
 */
public interface ResultRepositoryCustom {
    List<ResultStockSyncDto> findStockSyncInList(Long resultSrl, Date startDate, Date endDate);
    List<ResultStockSyncDto> findStockSyncOutList(Long resultSrl, Date startDate, Date endDate);
}

