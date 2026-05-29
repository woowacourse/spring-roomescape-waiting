package roomescape.service.result;

import java.time.LocalDateTime;
import roomescape.domain.Reservation;

public record ReservationResult(
        long id,
        String name,
        String status,
        LocalDateTime createdAt
) {

    public static ReservationResult from(Reservation reservation) {
        if (reservation == null) {
            return null;
        }
        return new ReservationResult(
                reservation.getId(),
                reservation.getName(),
                reservation.getStatus().name(),
                reservation.getCreatedAt()
        );
    }
}
