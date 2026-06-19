package ge.tsepesh.motorental.client;

import ge.tsepesh.motorental.dto.yookassa.YooKassaCreatePaymentRequest;
import ge.tsepesh.motorental.dto.yookassa.YooKassaPaymentResponse;
import ge.tsepesh.motorental.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Низкоуровневый HTTP-клиент для YooKassa REST API v3.
 * Каждый метод соответствует одному эндпоинту API.
 */
@Component
@Slf4j
public class YooKassaApiClient {

    private static final String PAYMENTS_PATH = "/payments";
    private static final String IDEMPOTENCE_KEY_HEADER = "Idempotence-Key";

    private final RestClient restClient;

    public YooKassaApiClient(@Qualifier("yooKassaRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * POST /v3/payments — создаёт новый платёж.
     *
     * @param request        тело запроса
     * @param idempotenceKey уникальный ключ для идемпотентности (UUID или строка)
     * @return объект созданного платежа с confirmation_url внутри
     */
    public YooKassaPaymentResponse createPayment(YooKassaCreatePaymentRequest request,
                                                  String idempotenceKey) {
        log.debug("Creating YooKassa payment, idempotence-key={}", idempotenceKey);
        try {
            return restClient.post()
                    .uri(PAYMENTS_PATH)
                    .header(IDEMPOTENCE_KEY_HEADER, idempotenceKey)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new PaymentException(
                                "YooKassa createPayment failed: HTTP " + res.getStatusCode());
                    })
                    .body(YooKassaPaymentResponse.class);
        } catch (RestClientException e) {
            throw new PaymentException("YooKassa API connection error during createPayment", e);
        }
    }

    /**
     * GET /v3/payments/{payment_id} — актуальный статус платежа.
     * Используется для проверки подлинности webhook-уведомления и при polling.
     *
     * @param paymentId идентификатор платежа в YooKassa
     * @return актуальный объект платежа
     */
    public YooKassaPaymentResponse getPayment(String paymentId) {
        log.debug("Fetching YooKassa payment status, paymentId={}", paymentId);
        try {
            return restClient.get()
                    .uri(PAYMENTS_PATH + "/{id}", paymentId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new PaymentException(
                                "YooKassa getPayment failed: HTTP " + res.getStatusCode()
                                        + ", paymentId=" + paymentId);
                    })
                    .body(YooKassaPaymentResponse.class);
        } catch (RestClientException e) {
            throw new PaymentException("YooKassa API connection error during getPayment: " + paymentId, e);
        }
    }
}
