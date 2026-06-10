package roomescape.dto.projection;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public record ReservationOrderProjection(Reservation reservation, Long order) {

    public Long getId() {
        return reservation.getId();
    }

    public String getName() {
        return reservation.getName();
    }

    public LocalDate getDate() {
        return reservation.getDate();
    }

    public ReservationTime getTime() {
        return reservation.getTime();
    }

    public Theme getTheme() {
        return reservation.getTheme();
    }

    public Long getOrder() {
        return order;
    }
}
