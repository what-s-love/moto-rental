package ge.tsepesh.motorental.dto.booking;

import ge.tsepesh.motorental.enums.BookingStatus;
import lombok.Data;

@Data
public class BookingUpdateDto {
    private BookingStatus status;
}