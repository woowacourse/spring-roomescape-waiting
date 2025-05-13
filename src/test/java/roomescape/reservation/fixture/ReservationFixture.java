package roomescape.reservation.fixture;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.dto.ReservationRequestDto;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.user.domain.User;

public class ReservationFixture {

    public static Reservation create(LocalDate date, ReservationTime reservationTime, Theme theme,
                                     User user) {
        return Reservation.of(date, reservationTime, theme, user);
    }

    public static ReservationRequestDto createRequestDto(LocalDate date, Long timeId, Long themeId) {
        return new ReservationRequestDto(date, timeId, themeId);
    }
}
