package com.pieceofcake.piece_service.piece.presentation;

import com.pieceofcake.piece_service.common.entity.BaseResponseEntity;
import com.pieceofcake.piece_service.common.entity.BaseResponseStatus;
import com.pieceofcake.piece_service.piece.application.PieceProductServiceImpl;
import com.pieceofcake.piece_service.piece.dto.in.CreatePieceProductLikeRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.CreatePieceProductRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.GetPieceProductUuidListRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.UpdatePieceProductRequestDto;
import com.pieceofcake.piece_service.piece.dto.out.GetLikedPieceProductResponseDto;
import com.pieceofcake.piece_service.piece.dto.out.GetMarketPriceResponseDto;
import com.pieceofcake.piece_service.piece.vo.in.CreatePieceProductLikeRequestVo;
import com.pieceofcake.piece_service.piece.vo.in.CreatePieceProductRequestVo;
import com.pieceofcake.piece_service.piece.vo.in.UpdatePieceProductRequestVo;
import com.pieceofcake.piece_service.piece.vo.out.GetLikedPieceProductResponseVo;
import com.pieceofcake.piece_service.piece.vo.out.GetMarketPriceResponseVo;
import com.pieceofcake.piece_service.piece.vo.out.GetPieceProductUuidListResponseVo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("api/v1/piece/product")
@RequiredArgsConstructor
@RestController
public class PieceProductController {

    private final PieceProductServiceImpl pieceProductService;

    @Operation(summary = "조각 상품 생성")
    @PostMapping
    public BaseResponseEntity<Void> createPieceProduct(@RequestBody CreatePieceProductRequestVo createPieceProductRequestVo) {
        pieceProductService.createPieceProduct(CreatePieceProductRequestDto.from(createPieceProductRequestVo));
        return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
    }

    @Operation(summary = "조각 상품 수정")
    @PutMapping
    public BaseResponseEntity<Void> updatePieceProduct(@RequestBody UpdatePieceProductRequestVo updatePieceProductRequestVo) {
        pieceProductService.updatePieceProduct(UpdatePieceProductRequestDto.from(updatePieceProductRequestVo));
        return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
    }

    @Operation(summary = "조각 상품 삭제")
    @DeleteMapping("/{pieceProductUuid}")
    public BaseResponseEntity<Void> deletePieceProduct(@PathVariable String pieceProductUuid) {
        pieceProductService.deletePieceProduct(pieceProductUuid);
        return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
    }

//    @Operation(summary = "조각 상품 uuid list 조회")
//    @GetMapping("/uuid-list")
//    public BaseResponseEntity<GetPieceProductUuidListResponseVo> getPieceProductUuidList(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(value = "isTrading", required = false) Boolean isTrading
//    ) {
//        return new BaseResponseEntity<>(
//                pieceProductService.getPieceProductUuidList(GetPieceProductUuidListRequestDto.of(page, size, isTrading))
//                        .toVo()
//        );
//    }

    @Operation(summary = "조각 상품 시장가 조회")
    @GetMapping("/market-price/{pieceProductUuid}")
    public BaseResponseEntity<GetMarketPriceResponseVo> getMarketPrice(@PathVariable String pieceProductUuid) {
        GetMarketPriceResponseDto marketPrice = pieceProductService.getMarketPrice(pieceProductUuid);
        return new BaseResponseEntity<>(marketPrice.toVo());
    }

    @Operation(summary = "조각 상품 찜 토글")
    @PostMapping("/like")
    public BaseResponseEntity<Void> createLikePieceProduct(
            @RequestHeader("X-Member-Uuid") String memberUuid,
            @RequestBody CreatePieceProductLikeRequestVo createPieceProductLikeRequestVo
    ) {
        pieceProductService.likePieceProduct(CreatePieceProductLikeRequestDto.from(memberUuid, createPieceProductLikeRequestVo));
        return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
    }

    @Operation(summary = "내가 찜한 조각상품 전체조회")
    @GetMapping("/like")
    public BaseResponseEntity<List<GetLikedPieceProductResponseVo>> getLikedPieceProduct(
            @RequestHeader("X-Member-Uuid") String memberUuid
    ) {
        return new BaseResponseEntity<>(
                pieceProductService.getLikedPieceProductList(memberUuid)
                .stream().map(GetLikedPieceProductResponseDto::toVo).toList()
        );
    }

    // 조각상품 찜 여부 조회
    @Operation(summary = "조각 상품 찜 여부 조회")
    @GetMapping("/like/{pieceProductUuid}")
    public BaseResponseEntity<Boolean> isLikedPieceProduct(
            @RequestHeader("X-Member-Uuid") String memberUuid,
            @PathVariable String pieceProductUuid
    ) {
        return new BaseResponseEntity<>(pieceProductService.isLikedPieceProduct(pieceProductUuid, memberUuid));
    }

}
