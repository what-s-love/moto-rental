package ge.tsepesh.motorental.dto.yookassa;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * Тело запроса POST /v3/payments для создания платежа (Умный платёж, Redirect).
 * Поля с null не сериализуются благодаря @JsonInclude.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record YooKassaCreatePaymentRequest(
        Amount amount,
        Boolean capture,
        Confirmation confirmation,
        String description,
        Map<String, String> metadata,
        Receipt receipt
) {

    public record Amount(
            String value,
            String currency
    ) {}

    public record Confirmation(
            String type,
            @JsonProperty("return_url") String returnUrl
    ) {}

    public record Receipt(
            Customer customer,
            List<ReceiptItem> items
    ) {}

    public record Customer(
            String email
    ) {}

    public record ReceiptItem(
            String description,
            String quantity,
            Amount amount,
            @JsonProperty("vat_code") int vatCode
    ) {}
}
