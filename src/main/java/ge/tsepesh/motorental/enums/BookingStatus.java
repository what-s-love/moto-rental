package ge.tsepesh.motorental.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

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

    public static final List<BookingStatus> ACTIVE_STATUSES =
            List.of(PENDING_PAYMENT, PAID, COMPLETED);

    public boolean isActive() {
        return ACTIVE_STATUSES.contains(this);
    }
}