package com.pieceofcake.piece_service.trade.application;

import com.pieceofcake.piece_service.trade.dto.out.GetOwnedPieceResponseDto;

import java.util.List;

public interface TradeService {

    List<GetOwnedPieceResponseDto> getOwnedPieceByMemberUuid(String memberUuid);

}
