package com.fetch.receipts.api;

import com.fetch.receipts.points.PointCalculation;
import com.fetch.receipts.model.Receipt;
import com.fetch.receipts.model.ReceiptsIdPointsGet200Response;
import com.fetch.receipts.model.ReceiptsProcessPost200Response;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.json.common.JsonReader;
import ru.tinkoff.kora.json.common.JsonWriter;
import ru.tinkoff.kora.validation.common.Validator;
import ru.tinkoff.kora.validation.common.Violation;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Component
public final class ReceiptsDelegate implements DefaultApiDelegate {
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final int RECEIPT_STORAGE_CAPACITY = 10_000;

    private final Validator<Receipt> receiptValidator;
    private final JsonWriter<Receipt> receiptWriter;
    private final JsonReader<Receipt> receiptReader;

    private final Cache<String, String> cache;

    public ReceiptsDelegate(Validator<Receipt> receiptValidator,
                            JsonWriter<Receipt> receiptWriter,
                            JsonReader<Receipt> receiptReader) {
        this.receiptValidator = receiptValidator;
        this.receiptWriter = receiptWriter;
        this.receiptReader = receiptReader;

        cache = CacheBuilder.newBuilder()
                .maximumSize(RECEIPT_STORAGE_CAPACITY)
                .build();
    }

    @Override
    public DefaultApiResponses.ReceiptsProcessPostApiResponse receiptsProcessPost(Receipt receipt) throws IOException {
        List<Violation> violations = receiptValidator.validate(receipt);
        if (!violations.isEmpty()) {
            return new DefaultApiResponses.ReceiptsProcessPostApiResponse.ReceiptsProcessPost400ApiResponse();
        }

        try {
            LocalTime.parse(receipt.purchaseTime(), TIME_FORMATTER);
        } catch (Exception e) {
            return new DefaultApiResponses.ReceiptsProcessPostApiResponse.ReceiptsProcessPost400ApiResponse();
        }

        String id = UUID.randomUUID().toString();
        cache.put(id, receiptWriter.toString(receipt));

        ReceiptsProcessPost200Response content = new ReceiptsProcessPost200Response(id);
        return new DefaultApiResponses.ReceiptsProcessPostApiResponse.ReceiptsProcessPost200ApiResponse(content);
    }

    @Override
    public DefaultApiResponses.ReceiptsIdPointsGetApiResponse receiptsIdPointsGet(String id) throws IOException {
        String receiptJson = cache.getIfPresent(id);
        if (receiptJson == null) {
            return new DefaultApiResponses.ReceiptsIdPointsGetApiResponse.ReceiptsIdPointsGet404ApiResponse();
        }
        Receipt receipt = receiptReader.read(receiptJson);

        long points = PointCalculation.calculatePoints(receipt);
        ReceiptsIdPointsGet200Response content = new ReceiptsIdPointsGet200Response().withPoints(points);
        return new DefaultApiResponses.ReceiptsIdPointsGetApiResponse.ReceiptsIdPointsGet200ApiResponse(content);
    }
}
