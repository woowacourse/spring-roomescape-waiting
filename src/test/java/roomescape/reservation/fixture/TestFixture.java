package roomescape.reservation.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
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

    public static Reservation makeReservation(final LocalDate date, final ReservationTime reservationTime,
                                              final Member member, final Theme theme) {
        return new Reservation(member, date, reservationTime, theme, ReservationStatus.RESERVED);
    }

    public static ReservationTime makeReservationTime(final long reservationTimeId) {
        return ReservationTime.withUnassignedId(TIME);
    }

    public static ReservationTime makeReservationTime(final LocalTime localTime) {
        return ReservationTime.withUnassignedId(localTime);
    }

    public static LocalDate makeFutureDate() {
        return LocalDate.now().plusDays(5);
    }

    public static Member makeMember() {
        return new Member("Mint", "mint@gmail.com", "password", MemberRole.USER);
    }
}
