package ge.tsepesh.motorental.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookingStatus {
    PENDING_PAYMENT("В ожидании оплаты"),
    PAID("Оплачено"),
    PAYMENT_FAILED("Ошибка оплаты"),
    EXPIRED("Просрочено"),
    CANCELLED("Отменено"),
    COMPLETED("Завершено");

    private final String displayName;
}