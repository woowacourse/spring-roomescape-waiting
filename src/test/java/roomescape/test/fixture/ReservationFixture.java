package roomescape.test.fixture;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.dto.request.ReservationRequestDto;

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
}
