package com.fetch.receipts.points;

import com.fetch.receipts.model.Receipt;

/**
 *     - One point for every alphanumeric character in the retailer name.
 */
public class RetailerNameCalculator implements PointCalculator {
    @Override
    public long getPoints(Receipt receipt) {
        long points = 0;
        for (char c : receipt.retailer().toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                points += 1;
            }
        }
        return points;
    }
}
