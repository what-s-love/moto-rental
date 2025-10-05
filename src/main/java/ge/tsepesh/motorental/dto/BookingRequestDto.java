package ge.tsepesh.motorental.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookingRequestDto {

    @NotNull(message = "Дата заезда обязательна")
    @Future(message = "Дата заезда должна быть в будущем")
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
}