package com.fetch.receipts.api;

import com.fetch.receipts.model.Item;
import com.fetch.receipts.model.Receipt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tinkoff.kora.validation.common.ValidationContext;
import ru.tinkoff.kora.validation.common.Validator;
import ru.tinkoff.kora.validation.common.Violation;

import java.time.LocalDate;
import java.util.List;

import static com.fetch.receipts.api.DefaultApiResponses.ReceiptsProcessPostApiResponse.ReceiptsProcessPost200ApiResponse;
import static com.fetch.receipts.api.DefaultApiResponses.ReceiptsProcessPostApiResponse.ReceiptsProcessPost400ApiResponse;
import static com.fetch.receipts.api.DefaultApiResponses.ReceiptsIdPointsGetApiResponse.ReceiptsIdPointsGet200ApiResponse;
import static com.fetch.receipts.api.DefaultApiResponses.ReceiptsIdPointsGetApiResponse.ReceiptsIdPointsGet404ApiResponse;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
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

    static Receipt createMAndMReceipt() {
        String retailer = "M&M Corner Market";
        LocalDate purchaseDate = LocalDate.of(2022, 3, 20);
        String purchaseTime = "14:33";
        List<Item> items = List.of(
                new Item("Gatorade", "2.25"),
                new Item("Gatorade", "2.25"),
                new Item("Gatorade", "2.25"),
                new Item("Gatorade", "2.25")
        );
        String total = "9.00";
        return new Receipt(retailer, purchaseDate, purchaseTime, items, total);
    }

    static Receipt createIncorrectTimeReceipt() {
        String retailer = "M&M Corner Market";
        LocalDate purchaseDate = LocalDate.of(2022, 3, 20);
        String purchaseTime = "14:33:12";
        List<Item> items = List.of();
        String total = "9.00";
        return new Receipt(retailer, purchaseDate, purchaseTime, items, total);
    }

    static Receipt createTargetReceipt() {
        String retailer = "Target";
        LocalDate purchaseDate = LocalDate.of(2022, 1, 1);
        String purchaseTime = "13:01";
        List<Item> items = List.of(
                new Item("Mountain Dew 12PK", "6.49"),
                new Item("Emils Cheese Pizza", "12.25"),
                new Item("Knorr Creamy Chicken", "1.26"),
                new Item("Doritos Nacho Cheese", "3.35"),
                new Item("   Klarbrunn 12-PK 12 FL OZ  ", "12.00")
        );
        String total = "35.35";
        return new Receipt(retailer, purchaseDate, purchaseTime, items, total);
    }

    @Test
    void equalityTest() {
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
        assertTrue(receiptsDelegate.receiptsIdPointsGet("this id does not exist") instanceof ReceiptsIdPointsGet404ApiResponse);
    }

    @Test
    void testMAndM() {
        Receipt receipt = createMAndMReceipt();

        ReceiptsDelegate receiptsDelegate = new ReceiptsDelegate(NO_ISSUES);
        ReceiptsProcessPost200ApiResponse idResponse = (ReceiptsProcessPost200ApiResponse)receiptsDelegate.receiptsProcessPost(receipt);
        String id = idResponse.content().id();

        ReceiptsIdPointsGet200ApiResponse pointsResponse = (ReceiptsIdPointsGet200ApiResponse) receiptsDelegate.receiptsIdPointsGet(id);
        Long points = pointsResponse.content().points();

        assertEquals(MM_POINTS, points);
    }

    @Test
    void testWithIssuesReturns400() {
        Receipt receipt = createMAndMReceipt();

        ReceiptsDelegate receiptsDelegate = new ReceiptsDelegate(ISSUES);

        assertTrue(receiptsDelegate.receiptsProcessPost(receipt) instanceof ReceiptsProcessPost400ApiResponse);
    }

    @Test
    void testWrongTimeFormatReturns400() {
        Receipt receipt = createIncorrectTimeReceipt();

        ReceiptsDelegate receiptsDelegate = new ReceiptsDelegate(NO_ISSUES);

        assertTrue(receiptsDelegate.receiptsProcessPost(receipt) instanceof ReceiptsProcessPost400ApiResponse);
    }

    @Test
    void testTarget() {
        Receipt receipt = createTargetReceipt();

        ReceiptsDelegate receiptsDelegate = new ReceiptsDelegate(NO_ISSUES);
        ReceiptsProcessPost200ApiResponse idResponse = (ReceiptsProcessPost200ApiResponse)receiptsDelegate.receiptsProcessPost(receipt);
        String id = idResponse.content().id();

        ReceiptsIdPointsGet200ApiResponse pointsResponse = (ReceiptsIdPointsGet200ApiResponse) receiptsDelegate.receiptsIdPointsGet(id);
        Long points = pointsResponse.content().points();

        assertEquals(TARGET_POINTS, points);
    }
}
