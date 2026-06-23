package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

public record ReservationCreateResponse(
        Long id,
        String name,
        LocalDate date,
        TimeResponse time,
        ReservationStatus status,
        String orderId,
        Long amount
) {
    public static ReservationCreateResponse from(Reservation reservation) {
        return new ReservationCreateResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                TimeResponse.from(reservation.getTime()),
                reservation.getStatus(),
                null,
                null
        );
    }

    public ReservationCreateResponse withOrder(String orderId, Long amount) {
        return new ReservationCreateResponse(id, name, date, time, status, orderId, amount);
    }
}
