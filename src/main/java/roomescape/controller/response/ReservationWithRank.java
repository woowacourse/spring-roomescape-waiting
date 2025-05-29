package roomescape.controller.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;

public record ReservationWithRank(Long id, int rank, String name, LocalDate date, LocalTime startAt,
                                  String themeName, ReservationStatus status) {

    public static ReservationWithRank from(Reservation reservation, int rank) {
        return new ReservationWithRank(
                reservation.getId(),
                rank,
                reservation.getMember().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getTheme().getName(),
                reservation.getStatus()
        );
    }
}
