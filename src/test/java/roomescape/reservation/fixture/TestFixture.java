package roomescape.reservation.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class TestFixture {

    private static final LocalTime TIME = LocalTime.of(10, 0);

    public static Theme makeTheme(Long id) {
        return Theme.of(id, "추리", "셜록 추리 게임 with Danny", "image.png");
    }

    public static LocalDateTime makeTimeAfterOneHour() {
        return LocalDateTime.now().plusHours(1);
    }

    public static Reservation makeReservation(final Long reservationId, final long reservationTimeId) {
        ReservationTime reservationTime = makeReservationTime(reservationTimeId);
        return Reservation.of(reservationId, makeFutureDate(), makeMember(), reservationTime, makeTheme(1L));
    }

    public static ReservationTime makeReservationTime(final long reservationTimeId) {
        return ReservationTime.of(reservationTimeId, TIME);
    }

    public static LocalDate makeFutureDate() {
        return LocalDate.now().plusDays(5);
    }

    public static LocalDate makeNowDate() {
        return LocalDate.of(2025, 5, 12);
    }

    public static Member makeMember() {
        return Member.of(1L, "Mint", "mint@gmail.com", "password", MemberRole.USER);
    }
}
