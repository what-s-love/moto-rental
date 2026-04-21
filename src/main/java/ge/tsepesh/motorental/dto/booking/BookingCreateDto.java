package ge.tsepesh.motorental.dto.booking;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookingCreateDto {
    private Integer clientId;
    private String newClientName;
    private String newClientEmail;
    private String newClientPhone;
    private LocalDate rideDate;
    private Integer shiftId;
    private Integer routeId;
    private List<Integer> bikeIds;
    private Boolean isPrepaid;
    private Boolean generatePaymentLink;
}