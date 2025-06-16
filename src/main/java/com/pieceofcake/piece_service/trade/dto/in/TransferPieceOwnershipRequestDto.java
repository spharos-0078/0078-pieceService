package com.pieceofcake.piece_service.trade.dto.in;

import com.pieceofcake.piece_service.trade.entity.OwnedPiece;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class TransferPieceOwnershipRequestDto {

    private String pieceProductUuid;
    private String pieceUuid;
    private Long tradePrice;
    private String memberUuid;

    @Builder
    public TransferPieceOwnershipRequestDto(
            String pieceProductUuid, String pieceUuid, Long tradePrice, String memberUuid
    ) {
        this.pieceProductUuid = pieceProductUuid;
        this.pieceUuid = pieceUuid;
        this.tradePrice = tradePrice;
        this.memberUuid = memberUuid;
    }

    public static TransferPieceOwnershipRequestDto of(OwnedPiece ownedPiece, String memberUuid) {
        return TransferPieceOwnershipRequestDto.builder()
                .pieceProductUuid(ownedPiece.getPieceProductUuid())
                .pieceUuid(ownedPiece.getPieceUuid())
                .tradePrice(ownedPiece.getTradePrice())
                .memberUuid(memberUuid)
                .build();
    }

    public OwnedPiece toEntity() {
        return OwnedPiece.builder()
                .ownedPieceUuid(UUID.randomUUID().toString().substring(0, 32))
                .pieceProductUuid(pieceProductUuid)
                .pieceUuid(pieceUuid)
                .tradePrice(tradePrice)
                .memberUuid(memberUuid)
                .build();
    }
}
