package roomescape.service.dto.request;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public record ServiceReservationCreateRequest(
        String name,
        LocalDate date,
        Long timeId,
        Long themeId
) {
    public Reservation toEntity(ReservationTime reservationTime, Theme theme) {
        return new Reservation(name, date, reservationTime, theme);
    }
}
