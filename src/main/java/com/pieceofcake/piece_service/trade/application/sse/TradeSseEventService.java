package com.pieceofcake.piece_service.trade.application.sse;

import com.pieceofcake.piece_service.trade.dto.out.UpdateMarketPriceSseDto;
import com.pieceofcake.piece_service.trade.dto.out.UpdateQuotesSseDto;
import reactor.core.publisher.Flux;

public interface TradeSseEventService{
    Flux<UpdateQuotesSseDto> getQuotesUpdatesByPieceProductUuid(String pieceProductUuid);
    Flux<UpdateMarketPriceSseDto> getMatchedUpdatesByPieceProductUuid(String pieceProductUuid);
}
