package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationSummary(
        Long id,
        String name,
        LocalDate date,
        LocalTime startAt,
        String themeName,
        ReservationStatus status,
        String orderId,
        String paymentKey,
        Long amount
) {
}
