package roomescape.service.result;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.Reservation;

public record ReservationResult(
        Long id,
        MemberResult member,
        ThemeResult theme,
        LocalDate date,
        ReservationTimeResult time
) {
    public static ReservationResult from(Reservation reservation) {
        return new ReservationResult(
                reservation.getId(),
                MemberResult.from(reservation.getMember()),
                ThemeResult.from(reservation.getTheme()),
                reservation.getDate(),
                ReservationTimeResult.from(reservation.getTime())
        );
    }

    public static List<ReservationResult> from(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }
}
