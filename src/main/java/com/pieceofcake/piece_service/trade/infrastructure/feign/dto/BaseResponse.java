package com.pieceofcake.piece_service.trade.infrastructure.feign.dto;

import lombok.Getter;

@Getter
public class BaseResponse<T> {
    private String httpStatus;
    private boolean isSuccess;
    private String message;
    private int code;
    private T result;
}
