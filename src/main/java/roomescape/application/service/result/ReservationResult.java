package roomescape.application.service.result;

import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.Reservation;

public record ReservationResult(
        long id,
        String name,
        String status,
        LocalDateTime createdAt
) {

    public static ReservationResult from(Reservation reservation) {
        Objects.requireNonNull(reservation);
        return new ReservationResult(
                reservation.getId(),
                reservation.getName(),
                reservation.getStatus().name(),
                reservation.getCreatedAt()
        );
    }
}
