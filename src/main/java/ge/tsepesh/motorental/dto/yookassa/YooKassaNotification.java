package ge.tsepesh.motorental.dto.yookassa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Тело входящего уведомления (webhook) от YooKassa.
 * <p>
 * type  — всегда "notification".
 * event — название события, например "payment.succeeded", "payment.canceled".
 * object — полный объект платежа на момент события.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record YooKassaNotification(
        String type,
        String event,
        YooKassaPaymentResponse object
) {}
