package roomescape.service.result;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;

import java.time.LocalDate;
import java.util.List;

public record ReservationResult(
        Long id,
        MemberResult memberResult,
        LocalDate date,
        ReservationTimeResult time,
        ThemeResult theme,
        ReservationStatus status
) {
    public static ReservationResult from(Reservation reservation) {
        return new ReservationResult(
                reservation.getId(),
                MemberResult.from(reservation.getMember()),
                reservation.getDate(),
                ReservationTimeResult.from(reservation.getTime()),
                ThemeResult.from(reservation.getTheme()),
                reservation.getStatus());
    }

    public static List<ReservationResult> from(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }
}
