package roomescape.service.result;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.domain.Reservation;

public record ReservationResult(
        Long id,
        String memberName,
        String themeName,
        LocalDate date,
        LocalTime time
) {
    public static ReservationResult from(Reservation reservation) {
        return new ReservationResult(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt()
        );
    }

    public static List<ReservationResult> from(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }
}
