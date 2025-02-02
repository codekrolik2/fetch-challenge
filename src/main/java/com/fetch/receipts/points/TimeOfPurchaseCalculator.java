package com.fetch.receipts.points;

import com.fetch.receipts.model.Receipt;

import java.time.LocalTime;

import static com.fetch.receipts.api.ReceiptsDelegate.TIME_FORMATTER;

/**
 *     - 10 points if the time of purchase is after 2:00pm and before 4:00pm.
 */
public class TimeOfPurchaseCalculator implements PointCalculator {
    static final LocalTime START = LocalTime.of(14, 0); // 2:00 PM
    static final LocalTime END = LocalTime.of(16, 0);   // 4:00 PM

    @Override
    public long getPoints(Receipt receipt) {
        LocalTime time = LocalTime.parse(receipt.purchaseTime(), TIME_FORMATTER);
        if (!time.isBefore(START) && time.isBefore(END)) {
            return 10L;
        }
        return 0L;
    }
}
