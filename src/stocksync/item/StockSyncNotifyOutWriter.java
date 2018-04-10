package stocksync.item;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 물류 재고데이터 출고 자바 스프링 배치 샘플 코드
 */
@Component
@Scope("step")
public class StockSyncNotifyOutWriter extends AbstractItemWriter<ResultStockSyncDto> {

    private static final Logger logger = LoggerFactory.getLogger(StockSyncNotifyInWriter.class);
    private static final String REDIS_KEY_OUT = "wms_stock_sync_max_srl";
    private static final Integer CHUNK_SIZE = 1000;
    private static Gson gson = new Gson();

    @Autowired
    WmsApiClient wmsApiClient;
    @Autowired
    StockSyncTool stockSyncTool;


    @Override
    protected void doWrite(List<? extends ResultStockSyncDto> list) throws Exception {
        try {
            // 재고 동기화 API 호출
            List<ResultStockSyncDto> notifyInStockSyncDtoList = list.stream()
                    .collect(Collectors.toList());

            List<List<ResultStockSyncDto>> chunkedResultStockSyncDtoList = Lists.partition(Lists.newArrayList(notifyInStockSyncDtoList), CHUNK_SIZE);
            chunkedResultStockSyncDtoList.forEach(stockSyncList -> {
                List<NotifyInAndOutRequest> notifyReqList = stockSyncTool.convertNotifyReqFromResultStockSyncDto(stockSyncList);
                getStockStyncNotifyOut(notifyReqList);
            });

            Long resultSrl = stockSyncTool.getLatestResultSrl(list);
            stockSyncTool.updateRedisKey(resultSrl, REDIS_KEY_OUT);

            logger.info("StockSyncNotifyOutWriter: Redis에 저장된 resultSrl[{}]", String.valueOf(resultSrl));

        } catch (Exception e) {
            logger.error("Error in StockSyncNotifyOutWriter: {}", e.getMessage());
            throw new WmsException(ErrorMessages.서버연동오류);
        }

    }

    private void getStockStyncNotifyOut(List<NotifyInAndOutRequest> notifyInAndOutRequestList) {
        // API 호출
        try {


        } catch (LogisApiResponseException e) {

        }
    }
}
