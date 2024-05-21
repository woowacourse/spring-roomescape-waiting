package roomescape.fixture;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import roomescape.member.domain.Member;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Waiting;
import roomescape.reservation.model.Theme;

public class WaitingFixture {

    public static Waiting getOneWithTheme(final Theme theme) {
        return new Waiting(
                null,
                MemberFixture.getOne(),
                LocalDate.parse("3000-10-10"),
                ReservationTimeFixture.getOne(),
                theme
        );
    }

    public static Waiting getOneWithMember(final Member member) {
        return new Waiting(
                null,
                member,
                LocalDate.parse("3000-10-10"),
                ReservationTimeFixture.getOne(),
                ThemeFixture.getOne()
        );
    }

    public static Waiting getOneWithDateTimeTheme(final LocalDate date,
                                                      final ReservationTime reservationTime,
                                                      final Theme theme) {
        return new Waiting(
                null,
                MemberFixture.getOne(),
                date,
                reservationTime,
                theme
        );
    }

    public static Waiting getOneWithTimeTheme(final ReservationTime reservationTime,
                                                  final Theme theme) {
        return new Waiting(
                null,
                MemberFixture.getOne(),
                LocalDate.parse("3000-10-10"),
                reservationTime,
                theme
        );
    }

    public static Waiting getOneWithMemberTimeTheme(final Member member, final ReservationTime reservationTime,
                                                        final Theme theme) {
        return new Waiting(
                null,
                member,
                LocalDate.parse("3000-10-10"),
                reservationTime,
                theme
        );
    }

    public static List<Waiting> get(final int count) {
        final List<Waiting> reservations = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            reservations.add(new Waiting(
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
