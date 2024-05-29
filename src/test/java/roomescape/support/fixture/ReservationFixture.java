package roomescape.support.fixture;

import java.time.LocalDate;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public class ReservationFixture {

    public static Reservation create(Member member, ReservationTime time, Theme theme) {
        return create("2024-05-23", member, time, theme);
    }

    public static Reservation create(String date, Member member, ReservationTime time, Theme theme) {
        return create(LocalDate.parse(date), member, time, theme);
    }

    public static Reservation create(LocalDate date, Member member, ReservationTime time, Theme theme) {
        return new Reservation(date, member, time, theme);
    }
}
