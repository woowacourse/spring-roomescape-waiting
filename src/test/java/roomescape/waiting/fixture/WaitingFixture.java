package roomescape.waiting.fixture;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.user.domain.User;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.dto.WaitingRequestDto;

public class WaitingFixture {

    public static WaitingRequestDto createReqDto(LocalDate date, Long timeId, Long themeId) {
        return new WaitingRequestDto(date, timeId, themeId);
    }

    public static Waiting create(LocalDate date, ReservationTime reservationTime, Theme theme, User user) {
        return new Waiting(date, reservationTime, theme, user);
    }

    public static Waiting createByReservation(Reservation reservation) {
        return create(reservation.getDate(), reservation.getReservationTime(), reservation.getTheme(),
                reservation.getUser());
    }
}
