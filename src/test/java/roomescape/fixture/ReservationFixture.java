package roomescape.fixture;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import roomescape.member.domain.Member;
import roomescape.reservation.model.Reservation;
import roomescape.reservationtime.model.ReservationTime;
import roomescape.theme.model.Theme;

public class ReservationFixture {

    public static Reservation getOne() {
        return new Reservation(
                null,
                MemberFixture.getOne(),
                LocalDate.parse("3000-10-10"),
                ReservationTimeFixture.getOne(),
                ThemeFixture.getOne()
        );
    }

    public static Reservation getOneWithTheme(final Theme theme) {
        return new Reservation(
                null,
                MemberFixture.getOne(),
                LocalDate.parse("3000-10-10"),
                ReservationTimeFixture.getOne(),
                theme
        );
    }

    public static Reservation getOneWithMember(final Member member) {
        return new Reservation(
                null,
                member,
                LocalDate.parse("3000-10-10"),
                ReservationTimeFixture.getOne(),
                ThemeFixture.getOne()
        );
    }

    public static Reservation getOneWithDateTimeTheme(final LocalDate date,
                                                      final ReservationTime reservationTime,
                                                      final Theme theme) {
        return new Reservation(
                null,
                MemberFixture.getOne(),
                date,
                reservationTime,
                theme
        );
    }

    public static Reservation getOneWithTimeTheme(final ReservationTime reservationTime,
                                                  final Theme theme) {
        return new Reservation(
                null,
                MemberFixture.getOne(),
                LocalDate.parse("3000-10-10"),
                reservationTime,
                theme
        );
    }

    public static Reservation getOneWithMemberTimeTheme(final Member member, final ReservationTime reservationTime,
                                                        final Theme theme) {
        return new Reservation(
                null,
                member,
                LocalDate.parse("3000-10-10"),
                reservationTime,
                theme
        );
    }

    public static List<Reservation> get(final int count) {
        final List<Reservation> reservations = new ArrayList<>();
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
