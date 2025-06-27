package com.pieceofcake.piece_service.trade.infrastructure.feign.client;

import com.pieceofcake.piece_service.trade.infrastructure.feign.dto.CreateBoardRequestFeignDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "board-service", url = "${EC2_HOST}:8000")
public interface BoardFeignClient {

    @PostMapping("/board-service/api/v1/board/community")
    void createCommunityBoard(@RequestBody CreateBoardRequestFeignDto createBoardRequestFeignDto);

}
