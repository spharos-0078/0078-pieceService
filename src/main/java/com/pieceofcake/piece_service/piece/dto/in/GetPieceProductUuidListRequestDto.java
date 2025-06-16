package com.pieceofcake.piece_service.piece.dto.in;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Getter
public class GetPieceProductUuidListRequestDto {
    private Pageable pageable;
    private Boolean isTrading;

    @Builder
    public GetPieceProductUuidListRequestDto(Pageable pageable, Boolean isTrading) {
        this.pageable = pageable;
        this.isTrading = isTrading;
    }

    public static GetPieceProductUuidListRequestDto of(int page, int size, Boolean isTrading){
        return GetPieceProductUuidListRequestDto.builder()
                .pageable(PageRequest.of(page, size))
                .isTrading(isTrading)
                .build();
    }
}
