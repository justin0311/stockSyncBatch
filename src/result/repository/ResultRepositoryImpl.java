package result.repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Comparator.comparingLong;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.toList;

/**
 * 실적 데이터 JPA 구현 클래스
 */
public class ResultRepositoryImpl {

    @Override
    public List<ResultStockSyncDto> findStockSyncInList(Long resultSrl, Date startDate, Date endDate) {

        BooleanBuilder inBuilder = getBuilder(resultSrl, startDate, endDate,
                result.statementNo.substring(0, 2).ne(IP),
                result.statementNo.eq(receiptHeader.statementNo), result.stockAmt.gt(0));

        BooleanBuilder rtBuilder = getBuilder(resultSrl, startDate, endDate,
                result.statementNo.eq(returnHeader.statementNo),
                result.stockTotalAmtUnit.gt(0));

        BooleanBuilder mvBuilder = getBuilder(resultSrl, startDate, endDate,
                result.statementNo.substring(0, 2).eq(ResultStockSyncFlag.MV.toString()),
                ExpressionUtils.or(result.moveTo.eq(A).and(result.stockAmt.gt(0)), result.moveTo.ne(A).and(result.stockAmt.lt(0))));

        BooleanBuilder ajBuilder = getBuilder(resultSrl, startDate, endDate,
                result.statementNo.substring(0, 2).eq(ResultStockSyncFlag.AJ.toString()),
                result.moveTo.eq(A), result.moveFrom.eq(A), result.stockTotalAmtUnit.gt(0));

        List<ResultStockSyncDto> inResultList = getResultStockSyncDtoList(ResultStockSyncFlag.IN, result.stockAmt, inBuilder, result, receiptHeader);
        List<ResultStockSyncDto> rtResultList = getResultStockSyncDtoList(ResultStockSyncFlag.RT, result.stockTotalAmtUnit, rtBuilder, result, returnHeader);
        List<ResultStockSyncDto> mvResultList = getResultStockSyncDtoList(ResultStockSyncFlag.MV, getMvSkuQtyCase(), mvBuilder, result);
        List<ResultStockSyncDto> ajResultList = getResultStockSyncDtoList(ResultStockSyncFlag.AJ, result.stockTotalAmtUnit, ajBuilder, result);


        return Stream.of(inResultList, rtResultList, mvResultList, ajResultList)
                .flatMap(Collection::stream)
                .sorted(nullsLast(comparingLong(ResultStockSyncDto::getResultSrl)))
                .collect(toList());
    }

    @Override
    public List<ResultStockSyncDto> findStockSyncOutList(Long resultSrl, Date startDate, Date endDate) {
        BooleanBuilder odBuilder = getBuilder(resultSrl, startDate, endDate, result.statementNo.eq(outInfoHeader.statementNo), result.stockAmt.gt(0));

        BooleanBuilder mvBuilder = getBuilder(resultSrl, startDate, endDate,
                result.statementNo.substring(0,2).eq(ResultStockSyncFlag.MV.toString()),
                ExpressionUtils.or(result.moveTo.eq(A).and(result.stockAmt.lt(0)), result.moveTo.ne(A).and(result.stockAmt.gt(0))));

        BooleanBuilder ajBuilder = getBuilder(resultSrl, startDate, endDate,
                result.statementNo.substring(0,2).eq(ResultStockSyncFlag.AJ.toString()),
                result.moveFrom.eq(A), result.moveTo.eq(A), result.stockTotalAmtUnit.lt(0));

        List<ResultStockSyncDto> odResultList = getResultStockSyncDtoList(ResultStockSyncFlag.OD, result.stockAmt.multiply(-1), odBuilder, result, outInfoHeader);
        List<ResultStockSyncDto> mvResultList = getResultStockSyncDtoList(ResultStockSyncFlag.MV, getMvSkuQtyCase(), mvBuilder, result);
        List<ResultStockSyncDto> ajResultList = getResultStockSyncDtoList(ResultStockSyncFlag.AJ, result.stockTotalAmtUnit, ajBuilder, result);


        return Stream.of(odResultList, mvResultList, ajResultList)
                .flatMap(Collection::stream)
                .sorted(nullsLast(comparingLong(ResultStockSyncDto::getResultSrl)))
                .collect(toList());
    }

    private NumberExpression getMvSkuQtyCase() {
        return new CaseBuilder()
                .when(result.moveTo.eq(A))
                .then(result.stockAmt)
                .otherwise(result.stockAmt.multiply(-1));
    }

    private BooleanBuilder getBuilder(Long resultSrl, Date startDate, Date endDate, Predicate... predicates) {
        BooleanBuilder builder = new BooleanBuilder(ExpressionUtils.allOf(
                result.resultSrl.gt(resultSrl),
                result.wmsDate.goe(startDate),
                result.wmsDate.loe(endDate),
                result.centerCode.ne(WMWHSE3)));

        for (Predicate predicate : predicates) {
            builder.and(predicate);
        }

        return builder;
    }

    private List<ResultStockSyncDto> getResultStockSyncDtoList(ResultStockSyncFlag flag, NumberExpression skuQty, BooleanBuilder builder, EntityPath... entityPaths) {
        return jpaQueryFactory.select(Projections.fields(ResultStockSyncDto.class,
                Expressions.stringTemplate(flag.getFlag()).as("flag"),
                result.resultSrl,
                result.statementType,
                result.skuSrl,
                skuQty.as("skuQty"),
                result.centerCode))
                .from(entityPaths)
                .where(builder)
                .fetch();
    }
}
