package ge.tsepesh.motorental.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yookassa")
public record YooKassaProperties(
        String shopId,
        String secretKey,
        String apiUrl,
        String returnUrl,
        ReceiptItemProperties receipt
) {
    public record ReceiptItemProperties(
            String quantity
    ) {}
}
