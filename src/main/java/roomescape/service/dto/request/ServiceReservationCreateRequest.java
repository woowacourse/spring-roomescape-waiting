package roomescape.service.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Wait;

public record ServiceReservationCreateRequest(
        String name,
        LocalDate reservationDate,
        Long timeId,
        Long themeId
) {
    public Reservation toReservation(ReservationTime reservationTime, Theme theme) {
        return new Reservation(name, reservationDate, reservationTime, theme);
    }

    public Wait toWait(LocalDateTime createdAt, ReservationTime reservationTime, Theme theme) {
        return new Wait(createdAt, name, reservationDate, reservationTime, theme);
    }
}
