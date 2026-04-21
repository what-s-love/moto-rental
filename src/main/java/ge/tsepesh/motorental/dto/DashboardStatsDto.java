package ge.tsepesh.motorental.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsDto {
    private Long totalBookings;
    private Long paidCount;
    private Long expiredCount;
    private Long completedCount;

    private Long weekTotalBookings;
    private Long weekPendingPaymentCount;
    private Long weekPaidCount;
    private Long weekExpiredCount;
}