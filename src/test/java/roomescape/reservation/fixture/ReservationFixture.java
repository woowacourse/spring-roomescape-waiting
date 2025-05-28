package roomescape.reservation.fixture;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.dto.ReservationInfo;
import roomescape.reservation.domain.dto.ReservationRequestDto;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.user.domain.User;

public class ReservationFixture {

    public static Reservation create(LocalDate date, ReservationStatus status, ReservationTime reservationTime,
            Theme theme,
            User user) {
        return Reservation.of(date, status, reservationTime, theme, user);
    }

    public static Reservation createByBookedStatus(LocalDate date, ReservationTime reservationTime, Theme theme,
            User user) {
        return create(date, ReservationStatus.BOOKED, reservationTime, theme, user);
    }

    public static ReservationRequestDto createRequestDto(LocalDate date, Long timeId, Long themeId) {
        return new ReservationRequestDto(date, timeId, themeId);
    }

    public static ReservationInfo createReservationInfo(Reservation reservation) {
        return new ReservationInfo(reservation.getDate(), reservation.getReservationTime(), reservation.getTheme());
    }
}
