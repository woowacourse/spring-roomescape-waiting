package roomescape.service.dto.request;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;

public record ServiceReservationUpdateRequest(
        LocalDate date,
        Long timeId
) {
    public Reservation toEntity(Reservation beforeReservation, ReservationTime newReservationTime) {
        return new Reservation(beforeReservation.getId(), beforeReservation.getName(), date, newReservationTime,
                beforeReservation.getTheme());
    }
}
