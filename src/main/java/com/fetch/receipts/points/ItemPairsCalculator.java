package com.fetch.receipts.points;

import com.fetch.receipts.model.Receipt;

/**
 *     - 5 points for every two items on the receipt.
 */
public class ItemPairsCalculator implements PointCalculator {
    @Override
    public long getPoints(Receipt receipt) {
        return 5L * (receipt.items().size() / 2);
    }
}
