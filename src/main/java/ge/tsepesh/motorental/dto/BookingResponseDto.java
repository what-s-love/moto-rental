package ge.tsepesh.motorental.dto;

import ge.tsepesh.motorental.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponseDto {
    private Integer bookingId;
    private BigDecimal totalPrice;
    private String currency;
    private LocalDateTime expiresAt;
    private BookingStatus status;
    private Integer participantCount;
    private String paymentUrl;
    private String confirmationCode;
}