package com.pieceofcake.piece_service.trade.infrastructure;

import com.pieceofcake.piece_service.trade.entity.FailedPaymentLog;
import com.pieceofcake.piece_service.trade.entity.OwnedPiece;
import com.pieceofcake.piece_service.trade.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FailedPaymentLogRepository extends JpaRepository<FailedPaymentLog, Long> {

    List<FailedPaymentLog> findByStatus(PaymentStatus status);

}
