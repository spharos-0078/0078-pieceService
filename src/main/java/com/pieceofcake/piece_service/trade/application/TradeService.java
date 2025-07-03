package com.pieceofcake.piece_service.trade.application;

import com.pieceofcake.piece_service.trade.dto.in.CreateTradeRequestDto;
import com.pieceofcake.piece_service.trade.dto.out.*;
import com.pieceofcake.piece_service.trade.vo.out.GetPieceAverageResponseVo;

import java.util.List;

public interface TradeService {

    List<GetOwnedPieceResponseDto> getOwnedPieceByMemberUuid(String memberUuid);

//    List<GetAllPieceProductUuidResponseDto> getOwnedPieceProductUuidByMemberUuid(String memberUuid, String pieceProductUuid);

    GetOwnedPieceResponseDto getOwnedPieceByMemberAndProductUuid(String memberUuid, String pieceProductUuid);

    void createBuyReservation(String memberUuid, CreateTradeRequestDto createTradeRequestDto);

    void createSellReservation(String memberUuid, CreateTradeRequestDto createTradeRequestDto);

    List<GetAllPieceProductUuidResponseDto> getOwnedPieceProductUuidByMemberUuid(String memberUuid);

    GetPieceAverageResponseDto getPieceAverageByMemberAndPieceProductUuid(String memberUuid, String pieceProductUuid);

    List<GetOwnedMemberAndPieceQuantityResponseDto> getOwnedMemberAndQuantity(String pieceProductUuid);

    void cancelReservation(String memberUuid, String reservationUuid);

    List<GetTradeReservationUuidResponseDto> getReservationUuidByMemberUuid(String memberUuid);

    GetTradeReservationResponseDto getReservationByUuid(String memberUuid, String reservationUuid);
}