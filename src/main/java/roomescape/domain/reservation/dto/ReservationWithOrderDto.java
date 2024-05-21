package roomescape.domain.reservation.dto;

import java.time.LocalDate;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.theme.domain.Theme;

public record ReservationWithOrderDto(Reservation reservation, Long orderNumber) {
    public Long getId() {
        return reservation.getId();
    }

    public Theme getTheme() {
        return reservation.getTheme();
    }

    public LocalDate getDate() {
        return reservation.getDate();
    }

    public ReservationTime getTime() {
        return reservation.getTime();
    }
}
