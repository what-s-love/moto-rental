package ge.tsepesh.motorental.dto.booking;

import ge.tsepesh.motorental.dto.ClientDto;
import ge.tsepesh.motorental.dto.ParticipantDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO для создания бронирования администратором через форму /admin/bookings/create
 */
@Data
@Builder
public class BookingCreateAdminDto {

    @NotNull(message = "Дата заезда обязательна")
    private LocalDate date;

    @NotNull(message = "Смена обязательна")
    @Min(value = 1, message = "Некорректная смена")
    private Integer shiftId;

    @NotNull(message = "Маршрут обязателен")
    @Min(value = 1, message = "Некорректный маршрут")
    private Integer routeId;

    @Valid
    @NotNull(message = "Данные клиента обязательны")
    private ClientDto client;

    @Valid
    @NotEmpty(message = "Должен быть хотя бы один участник")
    @Size(max = 8, message = "Максимальное количество участников: 8")
    private List<ParticipantDto> participants;

    private Boolean isPrepaid;
}