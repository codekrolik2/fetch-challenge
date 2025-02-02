package com.fetch.receipts.points;

import com.fetch.receipts.model.Receipt;

import java.math.BigDecimal;

/**
 *     - 50 points if the total is a round dollar amount with no cents.
 *     - 25 points if the total is a multiple of 0.25.
 */
public class TotalCalculator implements PointCalculator {
    static final BigDecimal POINT_25 = new BigDecimal(".25");

    public static boolean isRoundDollarWithNoCents(BigDecimal num) {
        return num.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
    }

    public static boolean divisibleByPoint25(BigDecimal num) {
        return num.remainder(POINT_25).compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public long getPoints(Receipt receipt) {
        long points = 0;

        BigDecimal total = new BigDecimal(receipt.total());

        if (isRoundDollarWithNoCents(total)) {
            points += 50;
        }
        if (divisibleByPoint25(total)) {
            points += 25;
        }

        return points;
    }
}
