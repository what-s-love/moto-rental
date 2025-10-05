package ge.tsepesh.motorental.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientDto {
    @NotBlank(message = "Имя обязательно")
    @Size(max = 255, message = "Имя слишком длинное")
    private String name;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный email")
    @Size(max = 255, message = "Email слишком длинный")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Некорректный номер телефона")
    private String phone;

    @Size(max = 100, message = "Telegram ID слишком длинный")
    private String telegramId;
}
