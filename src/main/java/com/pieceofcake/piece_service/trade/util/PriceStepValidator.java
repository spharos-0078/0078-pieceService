package com.pieceofcake.piece_service.trade.util;

public class PriceStepValidator {

    public static boolean isValidPrice(long piecePrice) {
        return piecePrice % getPriceStep(piecePrice) == 0;
    }

    public static long getPriceStep(long piecePrice) {
        if (piecePrice < 1_000) return 1;
        if (piecePrice < 5_000) return 5;
        if (piecePrice < 10_000) return 10;
        if (piecePrice < 50_000) return 50;
        if (piecePrice < 100_000) return 100;
        if (piecePrice < 500_000) return 500;
        return 1_000;
    }

}
