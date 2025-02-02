package com.fetch.receipts.points;

import com.fetch.receipts.model.Receipt;

public interface PointCalculator {
    long getPoints(Receipt receipt);
}
