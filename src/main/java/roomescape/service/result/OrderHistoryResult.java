package roomescape.service.result;

import java.time.LocalDate;
import java.time.LocalTime;

public record OrderHistoryResult(
        Long reservationId,
        LocalDate date,
        LocalTime time,
        String themeName,
        String entryStatus,
        String orderId,
        String paymentKey,
        Long amount,
        String paymentStatus
) {
}
