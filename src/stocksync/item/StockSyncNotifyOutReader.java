package stocksync.item;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * 물류 재고데이터 출고 자바 스프링 배치 샘플 코드
 */
@Component
@Scope("step")
public class StockSyncNotifyOutReader extends AbstractItemReader<ResultStockSyncDto> {

    private static final Logger logger = LoggerFactory.getLogger(StockSyncNotifyInReader.class);
    private static final String REDIS_KEY_OUT = "...";
    private static final String REDIS_KEY_OUT_INTIALIZING_RESULTSRL = "...";
    private static final String REDIS_KEY_OUT_INITIALIZED = "...";

    @Autowired
    StockSyncTool stockSyncTool;

    @Value("#{jobParameters['startDate']}")
    String startDateStr;
    @Value("#{jobParameters['endDate']}")
    String endDateStr;
    @Value("#{jobParameters['resultSrl']}")
    String resultSrlStr;

    private List<ResultStockSyncDto> stockSyncOutList;
    private Date startDate;
    private Date endDate;
    private Long resultSrl;
    private int cursor = 0;

    @BeforeStep
    void beforeStep() {
        try {
            startDate = stockSyncTool.getDateFromString(startDateStr, DateUtil.getToday(-3));
            endDate = stockSyncTool.getDateFromString(endDateStr, DateUtil.getTodayDateTime());
            resultSrl = stockSyncTool.getResultSrl(resultSrlStr, REDIS_KEY_OUT, REDIS_KEY_OUT_INTIALIZING_RESULTSRL, REDIS_KEY_OUT_INITIALIZED);

            logger.info("StockSyncNotifyOutReader: StockSyncNotifyOutReader: startDate[{}] endDate[{}] resultSrl[{}]", startDate.toString(), endDate.toString(), String.valueOf(resultSrl));
            stockSyncOutList = stockSyncTool.getStockSyncOutList(resultSrl, startDate, endDate);

        } catch (ParseException e) {
            logger.error("입력 파라미터 날짜 형식이 잘못 입력되었습니다: 입력형식[yyyy-Mm-ddHH:mm:ss]  startDate[{}] endDate[{}]", startDateStr, endDateStr);
            throw new IllegalArgumentException("입력 파라미터 날짜 형식이 잘못 입력되었습니다: 입력형식[yyyy-Mm-ddHH:mm:ss]  startDate[" + startDateStr + "] endDate[" + endDateStr + "]");

        } catch (RedisConnectionFailureException e) {
            logger.error("StockSyncNotifyOutReader: Redis에서 받아온 resultSrl 값이 존재하지 않습니다. {}", e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    protected ResultStockSyncDto doRead() throws Exception {
        if (CollectionUtils.isEmpty(stockSyncOutList)) {
            logger.error("StockSyncNotifyOutReader: 대상건 없음");
            return null;
        }

        if (cursor < stockSyncOutList.size()) {
            return stockSyncOutList.get(cursor++);
        }

        return null;
    }
}

