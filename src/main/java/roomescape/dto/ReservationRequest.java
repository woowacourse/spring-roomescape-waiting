package roomescape.dto;

import java.time.LocalDate;

import roomescape.domain.ReservationStatus;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;

public record ReservationRequest(LocalDate date, long timeId, long themeId) {
    public Reservation toReservation(Member member, ReservationTime reservationTime, Theme theme, ReservationStatus reservationStatus) {
        return new Reservation(this.date, reservationTime, theme, member, reservationStatus);
    }
}
