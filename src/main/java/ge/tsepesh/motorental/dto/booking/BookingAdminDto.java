package ge.tsepesh.motorental.dto.booking;

import ge.tsepesh.motorental.dto.ParticipantAdminDto;
import ge.tsepesh.motorental.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class BookingAdminDto {
    private Integer id;
    private LocalDate rideDate;
    private String shiftName;
    private LocalTime startTime;
    private LocalTime endTime;
    private String routeName;
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private BigDecimal totalPrice;
    private BookingStatus bookingStatus;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private List<ParticipantAdminDto> participants;
}