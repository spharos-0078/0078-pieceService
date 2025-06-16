package com.pieceofcake.piece_service.piece.infrastructure;

import com.pieceofcake.piece_service.piece.entity.QPieceProduct;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
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
public class PieceProductCustomImplRepository implements PieceProductCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<String> findProductUuidsByTradingStatus(Pageable pageable, Boolean isTrading) {
        QPieceProduct pieceProduct = QPieceProduct.pieceProduct;

        BooleanExpression condition = (isTrading != null)
                ? pieceProduct.isTrading.eq(isTrading)
                : null;


        List<String> result = queryFactory
                .select(pieceProduct.pieceProductUuid) // 예시
                .from(pieceProduct)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(new OrderSpecifier<>(Order.DESC, pieceProduct.id))
                .fetch();

        long total = Optional.ofNullable(
                queryFactory
                        .select(pieceProduct.count())
                        .from(pieceProduct)
                        .where(condition)
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(result, pageable, total);
    }
}
