package com.fetch.receipts.points;

import com.fetch.receipts.model.Receipt;

/**
 *     - 6 points if the day in the purchase date is odd.
 */
public class OddDayCalculator implements PointCalculator {
    @Override
    public long getPoints(Receipt receipt) {
        if (receipt.purchaseDate().getDayOfMonth() % 2 != 0) {
            return 6L;
        } else {
            return 0L;
        }
    }
}
