package com.fetch.receipts.api;

import com.fetch.receipts.model.Receipt;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.json.common.JsonReader;
import ru.tinkoff.kora.json.common.JsonWriter;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

import java.io.IOException;
import java.time.format.DateTimeParseException;

import static com.fetch.receipts.api.DefaultApiResponses.ReceiptsProcessPostApiResponse.ReceiptsProcessPost200ApiResponse;
import static com.fetch.receipts.api.DefaultApiResponses.ReceiptsIdPointsGetApiResponse.ReceiptsIdPointsGet200ApiResponse;
import static com.fetch.receipts.api.DefaultApiResponses.ReceiptsIdPointsGetApiResponse.ReceiptsIdPointsGet404ApiResponse;
import static org.junit.jupiter.api.Assertions.*;

@KoraAppTest(Application.class)
class ReceiptServiceTest {
    static final long MM_POINTS = 109;
    static final long TARGET_POINTS = 28;

    @TestComponent
    JsonReader<Receipt> receiptReader;
    @TestComponent
    JsonWriter<Receipt> receiptWriter;

    Receipt loadReceiptFromResource(String path) throws IOException {
        String json = Resources.toString(Resources.getResource(path), Charsets.UTF_8);
        return receiptReader.read(json);
    }

    Receipt createMAndMReceipt() throws IOException {
        return loadReceiptFromResource("mmReceipt.json");
    }


    Receipt createTargetReceipt() throws IOException {
        return loadReceiptFromResource("targetReceipt.json");
    }

    @Test
    void equalityTest() throws IOException {
        Receipt receipt1MM = createMAndMReceipt();
        Receipt receipt2MM = createMAndMReceipt();
        Receipt receipt1T = createTargetReceipt();
        Receipt receipt2T = createTargetReceipt();

        assertEquals(receipt1MM, receipt2MM);
        assertEquals(receipt1T, receipt2T);
        assertNotEquals(receipt1MM, receipt1T);
    }

    @Test
    void testNotFoundReturns404() throws IOException {
        ReceiptsDelegate receiptsDelegate = new ReceiptsDelegate(receiptWriter, receiptReader);
        assertInstanceOf(ReceiptsIdPointsGet404ApiResponse.class, receiptsDelegate.receiptsIdPointsGet("this id does not exist"));
    }

    @Test
    void testMAndM() throws IOException {
        Receipt receipt = createMAndMReceipt();

        ReceiptsDelegate receiptsDelegate = new ReceiptsDelegate(receiptWriter, receiptReader);
        ReceiptsProcessPost200ApiResponse idResponse = (ReceiptsProcessPost200ApiResponse)receiptsDelegate.receiptsProcessPost(receipt);
        String id = idResponse.content().id();

        ReceiptsIdPointsGet200ApiResponse pointsResponse = (ReceiptsIdPointsGet200ApiResponse) receiptsDelegate.receiptsIdPointsGet(id);
        Long points = pointsResponse.content().points();

        assertEquals(MM_POINTS, points);
    }

    @Test
    void testWrongTimeFormatReturns400() {
        assertThrows(DateTimeParseException.class, () -> loadReceiptFromResource("incorrectTimeReceipt.json"));
    }

    @Test
    void testTarget() throws IOException {
        Receipt receipt = createTargetReceipt();

        ReceiptsDelegate receiptsDelegate = new ReceiptsDelegate(receiptWriter, receiptReader);
        ReceiptsProcessPost200ApiResponse idResponse = (ReceiptsProcessPost200ApiResponse)receiptsDelegate.receiptsProcessPost(receipt);
        String id = idResponse.content().id();

        ReceiptsIdPointsGet200ApiResponse pointsResponse = (ReceiptsIdPointsGet200ApiResponse) receiptsDelegate.receiptsIdPointsGet(id);
        Long points = pointsResponse.content().points();

        assertEquals(TARGET_POINTS, points);
    }
}
