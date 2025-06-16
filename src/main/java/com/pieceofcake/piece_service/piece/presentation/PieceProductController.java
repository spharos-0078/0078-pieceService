package com.pieceofcake.piece_service.piece.presentation;

import com.pieceofcake.piece_service.common.entity.BaseResponseEntity;
import com.pieceofcake.piece_service.common.entity.BaseResponseStatus;
import com.pieceofcake.piece_service.piece.application.PieceProductServiceImpl;
import com.pieceofcake.piece_service.piece.dto.in.CreatePieceProductRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.GetPieceProductUuidListRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.UpdatePieceProductRequestDto;
import com.pieceofcake.piece_service.piece.vo.in.CreatePieceProductRequestVo;
import com.pieceofcake.piece_service.piece.vo.in.UpdatePieceProductRequestVo;
import com.pieceofcake.piece_service.piece.vo.out.GetPieceProductUuidListResponseVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("api/v1/piece/product")
@RequiredArgsConstructor
@RestController
public class PieceProductController {

    private final PieceProductServiceImpl pieceProductService;

    @PostMapping
    public BaseResponseEntity<Void> createPieceProduct(@RequestBody CreatePieceProductRequestVo createPieceProductRequestVo) {
        pieceProductService.createPieceProduct(CreatePieceProductRequestDto.from(createPieceProductRequestVo));
        return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
    }

    @PutMapping
    public BaseResponseEntity<Void> updatePieceProduct(@RequestBody UpdatePieceProductRequestVo updatePieceProductRequestVo) {
        pieceProductService.updatePieceProduct(UpdatePieceProductRequestDto.from(updatePieceProductRequestVo));
        return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
    }

    @DeleteMapping("/{pieceProductUuid}")
    public BaseResponseEntity<Void> deletePieceProduct(@PathVariable String pieceProductUuid) {
        pieceProductService.deletePieceProduct(pieceProductUuid);
        return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
    }

    @GetMapping("/uuid-list")
    public BaseResponseEntity<GetPieceProductUuidListResponseVo> getPieceProductUuidList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "isTrading", required = false) Boolean isTrading
    ) {
        return new BaseResponseEntity<>(
                pieceProductService.getPieceProductUuidList(GetPieceProductUuidListRequestDto.of(page, size, isTrading))
                        .toVo()
        );
    }

}
