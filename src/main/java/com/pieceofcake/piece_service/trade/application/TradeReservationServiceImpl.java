package com.pieceofcake.piece_service.trade.application;

import com.pieceofcake.piece_service.trade.dto.in.CreateTradeReservationRequestDto;
import com.pieceofcake.piece_service.trade.infrastructure.TradeReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TradeReservationServiceImpl implements TradeReservationService {

    private final TradeReservationRepository tradeReservationRepository;

    @Override
    public void createTradeReservation(CreateTradeReservationRequestDto createTradeReservationRequestDto, String memberUuid) {
        tradeReservationRepository.save(createTradeReservationRequestDto.toEntity());
    }
}
