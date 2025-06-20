package com.pieceofcake.piece_service.trade.infrastructure;

import com.pieceofcake.piece_service.trade.dto.out.GetTradedHistoryListResponseDto;
import com.pieceofcake.piece_service.trade.entity.QPieceTradedHistory;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PieceTradeCustomImplRepository implements PieceTradeCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<GetTradedHistoryListResponseDto> findDistinctHistoriesByPieceProductUuidAndMemberUuid(String pieceProductUuid,
                                                                                                      String memberUuid, Pageable pageable) {
        QPieceTradedHistory h = QPieceTradedHistory.pieceTradedHistory;

        // 하위 쿼리: historyUuid 기준으로 가장 작은 id만 추출
        List<Long> historyIds = queryFactory
                .select(h.id.min())
                .from(h)
                .where(
                        h.pieceProductUuid.eq(pieceProductUuid),
                        h.memberUuid.eq(memberUuid)
                )
                .groupBy(h.historyUuid)
                .orderBy(h.createdAt.min().desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 본문 쿼리: 위에서 뽑은 id로 실제 내용 조회
        List<GetTradedHistoryListResponseDto> content = queryFactory
                .select(Projections.constructor(
                        GetTradedHistoryListResponseDto.class,
                        h.historyUuid,
                        h.price,
                        h.tradeType,
                        h.quantity,
                        h.createdAt,
                        h.price.multiply(h.quantity)
                ))
                .from(h)
                .where(h.id.in(historyIds))
                .orderBy(h.createdAt.desc())
                .fetch();


        // count 쿼리
        Long total = Optional.ofNullable(queryFactory
                .select(h.historyUuid.countDistinct())
                .from(h)
                .where(
                        h.pieceProductUuid.eq(pieceProductUuid),
                        h.memberUuid.eq(memberUuid)
                )
                .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }
}
