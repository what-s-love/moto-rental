package ge.tsepesh.motorental.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "yookassa")
public record YooKassaProperties(
        String shopId,
        String secretKey,
        String apiUrl,
        BigDecimal prepaymentAmount,
        String currency,
        String returnUrl,
        ReceiptItemProperties receipt
) {
    public record ReceiptItemProperties(
            String itemDescription,
            Integer vatCode,
            String quantity
    ) {}
}
