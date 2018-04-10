package stocksync.model;

/**
 * 입고/출고 배치 내, 추출된 실적 데이터를 구분 짓는 FLAG
 */
public enum ResultStockSyncFlag {
    IN("'IN'", "입고"), RT("'RT'", "반품입고"), MV("'MV'", "재고이동"), AJ("'AJ'", "재고이관"), OD("'OD'", "반출출고");

    private String flag;
    private String flagName;

    ResultStockSyncFlag(String flag, String flagName) {
        this.flag = flag;
        this.flagName = flagName;
    }

    public String getFlag() {
        return this.flag;
    }

    public String getFlagName() {
        return this.flagName;
    }
}
