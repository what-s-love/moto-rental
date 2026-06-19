package ge.tsepesh.motorental.dto.yookassa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Ответ YooKassa на создание платежа и GET /v3/payments/{payment_id}.
 * Неизвестные поля ответа игнорируются.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record YooKassaPaymentResponse(
        String id,
        String status,
        Boolean paid,
        Amount amount,
        Confirmation confirmation,
        Map<String, String> metadata
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Amount(
            String value,
            String currency
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Confirmation(
            String type,
            @JsonProperty("confirmation_url") String confirmationUrl
    ) {}
}
