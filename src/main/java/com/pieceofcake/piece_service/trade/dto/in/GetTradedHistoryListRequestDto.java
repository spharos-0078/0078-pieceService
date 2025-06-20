package com.pieceofcake.piece_service.trade.dto.in;

import com.pieceofcake.piece_service.trade.dto.out.GetTradedHistoryListResponseDto;
import com.pieceofcake.piece_service.trade.vo.out.GetTradedHistoryListResponseVo;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Getter
public class GetTradedHistoryListRequestDto {
    private Pageable pageable;
    private String pieceProductUuid;
    private String memberUuid;

    @Builder
    public GetTradedHistoryListRequestDto(Pageable pageable, String pieceProductUuid, String memberUuid) {
        this.pageable = pageable;
        this.pieceProductUuid = pieceProductUuid;
        this.memberUuid = memberUuid;
    }

    public static GetTradedHistoryListRequestDto of(int page, int size, String pieceProductUuid, String memberUuid){
        return GetTradedHistoryListRequestDto.builder()
                .pageable(PageRequest.of(page, size))
                .pieceProductUuid(pieceProductUuid)
                .memberUuid(memberUuid)
                .build();
    }
}
