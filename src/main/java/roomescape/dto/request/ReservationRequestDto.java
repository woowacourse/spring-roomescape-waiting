package roomescape.dto.request;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.User;

public record ReservationRequestDto(LocalDate date, Long timeId, Long themeId) {

    public Reservation toEntity(ReservationTime reservationTime, Theme theme, User user) {
        return Reservation.createWithoutId(date, ReservationStatus.BOOKED, reservationTime, theme, user);
    }
}


