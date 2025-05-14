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
        return Theme.of("추리", "셜록 추리 게임 with Danny", "image.png");
    }

    public static LocalDateTime makeTimeAfterOneHour() {
        return LocalDateTime.now().plusHours(1);
    }

    public static Reservation makeReservation(final Long reservationId, final long reservationTimeId) {
        ReservationTime reservationTime = makeReservationTime(reservationTimeId);
        return Reservation.of(makeFutureDate(), makeMember(), reservationTime, makeTheme(1L));
    }

    public static Reservation makeReservation(final LocalDate date, final ReservationTime reservationTime,
                                              final Member member, final Theme theme) {
        return Reservation.of(date, member, reservationTime, theme);
    }

    public static ReservationTime makeReservationTime(final long reservationTimeId) {
        return ReservationTime.of(TIME);
    }

    public static ReservationTime makeReservationTime(final LocalTime localTime) {
        return ReservationTime.of(localTime);
    }

    public static LocalDate makeFutureDate() {
        return LocalDate.now().plusDays(5);
    }

    public static LocalDate makeNowDate() {
        return LocalDate.of(2025, 5, 12);
    }

    public static Member makeMember() {
        return Member.of("Mint", "mint@gmail.com", "password", MemberRole.USER);
    }
}
