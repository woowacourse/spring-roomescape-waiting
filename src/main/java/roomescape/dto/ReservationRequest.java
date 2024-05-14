package roomescape.dto;

import java.time.LocalDate;
import roomescape.domain.LoginMember;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public record ReservationRequest(LocalDate date, long timeId, long themeId) {
    public Reservation toReservation(LoginMember user, ReservationTime reservationTime, Theme theme) {
        return new Reservation(this.date, reservationTime, theme, user);
    }
}
