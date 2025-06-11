package com.pieceofcake.piece_service.trade.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/vi/piece-trade")
@RequiredArgsConstructor
@RestController
public interface OwnedPieceRepository {

    // 보유조각 CRUD
    @PostMapping

    // 조각거래 예약내역 CRUD


    // 조각 변동내역 CRUD

}
