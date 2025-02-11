package com.fetch.receipts.api;

import com.fetch.receipts.model.Receipt;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.json.common.JsonReader;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;
import ru.tinkoff.kora.validation.common.ValidationContext;
import ru.tinkoff.kora.validation.common.Validator;
import ru.tinkoff.kora.validation.common.Violation;

import java.io.IOException;
import java.util.List;

import static com.fetch.receipts.api.DefaultApiResponses.ReceiptsProcessPostApiResponse.ReceiptsProcessPost200ApiResponse;
import static com.fetch.receipts.api.DefaultApiResponses.ReceiptsProcessPostApiResponse.ReceiptsProcessPost400ApiResponse;
import static com.fetch.receipts.api.DefaultApiResponses.ReceiptsIdPointsGetApiResponse.ReceiptsIdPointsGet200ApiResponse;
import static com.fetch.receipts.api.DefaultApiResponses.ReceiptsIdPointsGetApiResponse.ReceiptsIdPointsGet404ApiResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@KoraAppTest(Application.class)
class ReceiptServiceTest {
    static final Validator<Receipt> NO_ISSUES = new Validator<>() {
        @Override
        public @NotNull List<Violation> validate(@Nullable Receipt value, @NotNull ValidationContext context) {
            return List.of();
        }
    };

    static final Validator<Receipt> ISSUES = new Validator<>() {
        @Override
        public @NotNull List<Violation> validate(@Nullable Receipt value, @NotNull ValidationContext context) {
            return List.of(new Violation() {
                @Override
                public @NotNull String message() { return "ISSUE"; }

                @Override
                public @NotNull ValidationContext.Path path() {
                    return new ValidationContext.Path() {
                        @Override public String value() { return "PATH"; }
                        @Override public ValidationContext.Path root() { return null; }
                    };
                }
            });
        }
    };

    static final long MM_POINTS = 109;
    static final long TARGET_POINTS = 28;

    @TestComponent
    JsonReader<Receipt> receiptReader;

    Receipt loadReceiptFromResource(String path) throws IOException {
        String json = Resources.toString(Resources.getResource(path), Charsets.UTF_8);
        return receiptReader.read(json);
    }

    Receipt createMAndMReceipt() throws IOException {
        return loadReceiptFromResource("mmReceipt.json");
    }

    Receipt createIncorrectTimeReceipt() throws IOException {
        return loadReceiptFromResource("incorrectTimeReceipt.json");
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
    void testNotFoundReturns404() {
        ReceiptsDelegate receiptsDelegate = new ReceiptsDelegate(NO_ISSUES);
        assertInstanceOf(ReceiptsIdPointsGet404ApiResponse.class, receiptsDelegate.receiptsIdPointsGet("this id does not exist"));
    }

    @Test
    void testMAndM() throws IOException {
        Receipt receipt = createMAndMReceipt();

        ReceiptsDelegate receiptsDelegate = new ReceiptsDelegate(NO_ISSUES);
        ReceiptsProcessPost200ApiResponse idResponse = (ReceiptsProcessPost200ApiResponse)receiptsDelegate.receiptsProcessPost(receipt);
        String id = idResponse.content().id();

        ReceiptsIdPointsGet200ApiResponse pointsResponse = (ReceiptsIdPointsGet200ApiResponse) receiptsDelegate.receiptsIdPointsGet(id);
        Long points = pointsResponse.content().points();

        assertEquals(MM_POINTS, points);
    }

    @Test
    void testWithIssuesReturns400() throws IOException {
        Receipt receipt = createMAndMReceipt();

        ReceiptsDelegate receiptsDelegate = new ReceiptsDelegate(ISSUES);

        assertInstanceOf(ReceiptsProcessPost400ApiResponse.class, receiptsDelegate.receiptsProcessPost(receipt));
    }

    @Test
    void testWrongTimeFormatReturns400() throws IOException {
        Receipt receipt = createIncorrectTimeReceipt();

        ReceiptsDelegate receiptsDelegate = new ReceiptsDelegate(NO_ISSUES);

        assertInstanceOf(ReceiptsProcessPost400ApiResponse.class, receiptsDelegate.receiptsProcessPost(receipt));
    }

    @Test
    void testTarget() throws IOException {
        Receipt receipt = createTargetReceipt();

        ReceiptsDelegate receiptsDelegate = new ReceiptsDelegate(NO_ISSUES);
        ReceiptsProcessPost200ApiResponse idResponse = (ReceiptsProcessPost200ApiResponse)receiptsDelegate.receiptsProcessPost(receipt);
        String id = idResponse.content().id();

        ReceiptsIdPointsGet200ApiResponse pointsResponse = (ReceiptsIdPointsGet200ApiResponse) receiptsDelegate.receiptsIdPointsGet(id);
        Long points = pointsResponse.content().points();

        assertEquals(TARGET_POINTS, points);
    }
}
