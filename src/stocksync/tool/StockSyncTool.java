package stocksync.tool;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 입고/출고 배치에서 공통적으로 사용되는 메소드 집합 입니다.
 */
@Component
public class StockSyncTool {

    private static final Logger logger = LoggerFactory.getLogger(StockSyncTool.class);
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");

    @Autowired
    TmonRedisFactory tmonRedisFactory;
    @Autowired
    ResultRepository resultRepository;

    public Date getDateFromString(String dateStr, Date date) throws ParseException {
        return StringUtils.isEmpty(dateStr) ? date : simpleDateFormat.parse(dateStr);
    }

    public Long getResultSrl(String resultSrlStr, String redisKeyStr, String initRedisKeyStr, String initializedRedisKeyStr) {
        Long resultSrl;
        if (StringUtils.isEmpty(resultSrlStr)) {
            resultSrl = getRedisResultSrl(redisKeyStr, initRedisKeyStr, initializedRedisKeyStr);

        } else {
            resultSrl = Longs.tryParse(resultSrlStr);
            if (Objects.isNull(resultSrl)) {
                logger.error("resultSrl 입력 파라미터 형식이 잘못 입력되었습니다. resultSrl[{}]", resultSrlStr);
                throw new NumberFormatException(String.format("resultSrl 입력 파라미터 형식이 잘못 입력되었습니다. resultSrl[%s]", resultSrlStr));
            }
        }
        return resultSrl;
    }

    public Long getLatestResultSrl(List<? extends ResultStockSyncDto> list) {
        return list.stream()
                .max(Comparator.comparingLong(ResultStockSyncDto::getResultSrl))
                .map(ResultStockSyncDto::getResultSrl)
                .orElseThrow(() -> new WmsException(ErrorMessages.데이터오류));
    }

    public void updateRedisKey(final Long resultSrl, String redisKey) {
        // update Redis with the latest resultSrl

        try {

        } catch (Exception e) {

        }
    }

    public List<NotifyInAndOutRequest> convertNotifyReqFromResultStockSyncDto(List<? extends ResultStockSyncDto> resultStockSyncOutDtoList) {

        return resultStockSyncOutDtoList.stream()
                .map(resultStockSync -> new NotifyInAndOutRequest(String.valueOf(resultStockSync.getResultSrl()), resultStockSync.getSkuSrl(), resultStockSync.getStatementType(), resultStockSync.getSkuQty(), resultStockSync.getCenterCode()))
                .collect(Collectors.toList());
    }

    public List<ResultStockSyncDto> getStockSyncInList(Long resultSrl, Date startDate, Date endDate) {
        return resultRepository.findStockSyncInList(resultSrl, startDate, endDate);
    }

    public List<ResultStockSyncDto> getStockSyncOutList(Long resultSrl, Date startDate, Date endDate) {
        return resultRepository.findStockSyncOutList(resultSrl, startDate, endDate);
    }

    private Long getRedisResultSrl(String redisKeyStr, String initRedisKeyStr, String initializedRedisKeyStr) {
        // Retrieve Key from Redis
        try {


        } catch (NullPointerException e) {

        }
    }
}
