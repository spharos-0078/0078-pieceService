package com.pieceofcake.piece_service.kafka.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PieceEvent {
    private String productUuid;
    private String pieceProductUuid;
    private Boolean isTrading;
}
