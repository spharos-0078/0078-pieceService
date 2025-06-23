package com.pieceofcake.piece_service.trade.dto.in;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Getter
public class GetMatchedHistoryListRequestDto {
    private Pageable pageable;
    private String pieceProductUuid;
    private String memberUuid;

    @Builder
    public GetMatchedHistoryListRequestDto(Pageable pageable, String pieceProductUuid, String memberUuid) {
        this.pageable = pageable;
        this.pieceProductUuid = pieceProductUuid;
        this.memberUuid = memberUuid;
    }

    public static GetMatchedHistoryListRequestDto of(int page, int size, String pieceProductUuid, String memberUuid) {
        return GetMatchedHistoryListRequestDto.builder()
                .pageable(PageRequest.of(page, size))
                .pieceProductUuid(pieceProductUuid)
                .memberUuid(memberUuid)
                .build();
    }
}
