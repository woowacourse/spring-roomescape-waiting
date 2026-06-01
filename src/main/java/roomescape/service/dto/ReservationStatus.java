package roomescape.service.dto;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.service.result.WaitingResult;

import java.time.LocalDate;

public record ReservationStatus(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        Status status,
        Long turn
) {
    public static ReservationStatus reserved(Reservation reservation) {
        return new ReservationStatus(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                Status.RESERVED,
                null
        );
    }

    public static ReservationStatus waiting(WaitingResult waiting) {
        return new ReservationStatus(
                waiting.id(),
                waiting.name(),
                waiting.date(),
                waiting.time(),
                waiting.theme(),
                Status.WAITING,
                waiting.turn()
        );
    }
}
