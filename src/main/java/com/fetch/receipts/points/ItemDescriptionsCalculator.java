package com.fetch.receipts.points;

import com.fetch.receipts.model.Item;
import com.fetch.receipts.model.Receipt;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *     - If the trimmed length of the item description is a multiple of 3, multiply the price by 0.2
 *          and round up to the nearest integer. The result is the number of points earned.
 */
public class ItemDescriptionsCalculator implements PointCalculator {
    static final BigDecimal POINT_2 = new BigDecimal("0.2");

    @Override
    public long getPoints(Receipt receipt) {
        long points = 0;
        for (Item item : receipt.items()) {
            points += getItemPoints(item);
        }
        return points;
    }

    public long getItemPoints(Item item) {
        if (item.shortDescription().trim().length() % 3 == 0) {
            BigDecimal price = new BigDecimal(item.price());
            BigDecimal points = POINT_2.multiply(price);
            return points.setScale(0, RoundingMode.CEILING).longValue();
        }
        return 0L;
    }
}
