package roomescape.fixture;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import roomescape.member.domain.Member;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Theme;

public class ReservationFixture {

    public static Reservation getOneWithTheme(Theme theme) {
        return new Reservation(
                null,
                MemberFixture.getOne(),
                LocalDate.parse("3000-10-10"),
                ReservationTimeFixture.getOne(),
                theme
        );
    }

    public static Reservation getOneWithMember(Member member) {
        return new Reservation(
                null,
                member,
                LocalDate.parse("3000-10-10"),
                ReservationTimeFixture.getOne(),
                ThemeFixture.getOne()
        );
    }

    public static Reservation getOneWithDateTimeTheme(LocalDate date,
                                                      ReservationTime reservationTime,
                                                      Theme theme) {
        return new Reservation(
                null,
                MemberFixture.getOne(),
                date,
                reservationTime,
                theme
        );
    }

    public static Reservation getOneWithTimeTheme(ReservationTime reservationTime,
                                                  Theme theme) {
        return new Reservation(
                null,
                MemberFixture.getOne(),
                LocalDate.parse("3000-10-10"),
                reservationTime,
                theme
        );
    }

    public static Reservation getOneWithMemberTimeTheme(Member member, ReservationTime reservationTime,
                                                        Theme theme) {
        return new Reservation(
                null,
                member,
                LocalDate.parse("3000-10-10"),
                reservationTime,
                theme
        );
    }

    public static List<Reservation> get(int count) {
        List<Reservation> reservations = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            reservations.add(new Reservation(
                            null,
                            MemberFixture.getOne(),
                            LocalDate.parse("3000-10-10"),
                            ReservationTimeFixture.getOne(),
                            ThemeFixture.getOne()
                    )
            );
        }

        return reservations;
    }
}
