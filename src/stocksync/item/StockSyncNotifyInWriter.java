package stocksync.item;


import java.util.*;
import java.util.stream.Collectors;

/**
 * 물류 재고데이터 입고 자바 스프링 배치 샘플 코드
 */
@Component
@Scope("step")
public class StockSyncNotifyInWriter extends AbstractItemWriter<ResultStockSyncDto> {

    private static final Logger logger = LoggerFactory.getLogger(StockSyncNotifyInWriter.class);
    private static final String REDIS_KEY_IN = "wms_stock_sync_notifyin_srl";
    private static final Integer CHUNK_SIZE = 1000;
    private static Gson gson = new Gson();

    @Autowired
    WmsApiClient wmsApiClient;
    @Autowired
    WmsApiCenterClient wmsApiCenterClient;
    @Autowired
    StockSyncTool stockSyncTool;


    @Override
    protected void doWrite(List<? extends ResultStockSyncDto> list) throws Exception {
        try {
            // 안전재고 API 호출
            Set<String> primaryCenterCodeSet = wmsApiCenterClient.getCenterGroupAll().stream()
                    .filter(center -> Use.Y.equals(center.getUse()))
                    .filter(center -> Use.Y.equals(center.getIsPrimary()))
                    .map(CenterGroup::getCenterCode)
                    .collect(Collectors.toSet());

            List<ResultStockSyncDto> inStockSyncDtoList = list.stream()
                    .filter(stockSync -> ResultStockSyncFlag.IN.toString().equals(stockSync.getFlag()))
                    .filter(stockSync -> primaryCenterCodeSet.contains(stockSync.getCenterCode()))
                    .collect(Collectors.toList());
            updateSafeStockAmount(inStockSyncDtoList);


            // 재고 동기화 API 호출
            List<ResultStockSyncDto> notifyInStockSyncDtoList = list.stream()
                    .collect(Collectors.toList());

            List<List<ResultStockSyncDto>> chunkedResultStockSyncDtoList = Lists.partition(Lists.newArrayList(notifyInStockSyncDtoList), CHUNK_SIZE);
            chunkedResultStockSyncDtoList.forEach(stockSyncList -> {
                List<NotifyInAndOutRequest> notifyReqList = stockSyncTool.convertNotifyReqFromResultStockSyncDto(stockSyncList);
                getStockStyncNotifyIn(notifyReqList);
            });

            Long resultSrl = stockSyncTool.getLatestResultSrl(list);
            stockSyncTool.updateRedisKey(resultSrl, REDIS_KEY_IN);

            logger.info("StockSyncNotifyInWriter: Redis에 저장된 resultSrl[{}]", String.valueOf(resultSrl));

        } catch (Exception e) {
            logger.error("Error in StockSyncNotifyInWriter: {}", e.getMessage());
            throw new WmsException(ErrorMessages.서버연동오류);
        }
    }

    private void updateSafeStockAmount(List<ResultStockSyncDto> inStockSyncDtoList) {
        if (CollectionUtils.isEmpty(inStockSyncDtoList)) {
            logger.error("StockSyncNotifyInWriter: updateSafeStockAmount. inStockSyncDtoList is empty.");
            return;
        }

        // SafeStock service_api 호출

    }

    private void getStockStyncNotifyIn(List<NotifyInAndOutRequest> notifyInAndOutRequestList) {
        // 재고동기화 API 호출
        try {


        } catch (LogisApiResponseException e) {

        }
    }
}
