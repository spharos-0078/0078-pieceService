package com.pieceofcake.piece_service.trade.application;

import com.pieceofcake.piece_service.trade.dto.out.GetOwnedPieceResponseDto;
import com.pieceofcake.piece_service.trade.entity.OwnedPiece;
import com.pieceofcake.piece_service.trade.infrastructure.OwnedPieceRepository;
import com.pieceofcake.piece_service.trade.infrastructure.TradeReservationRepository;
import com.pieceofcake.piece_service.trade.infrastructure.TradedHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TradeServiceImpl implements TradeService {

    private final OwnedPieceRepository ownedPieceRepository;
    private final TradedHistoryRepository tradedHistoryRepository;
    private final TradeReservationRepository tradeReservationRepository;

    @Override
    public List<GetOwnedPieceResponseDto> getOwnedPieceByMemberUuid(String memberUuid) {
        List<Object[]> result = ownedPieceRepository.countOwnedPiecesByMemberUuid(memberUuid);

        return result.stream().map(o -> {
            String pieceProductUuid = (String) o[0];
            Long count = (Long) o[1];
            return new GetOwnedPieceResponseDto(pieceProductUuid, count.intValue());
        }).toList();
    }
}
