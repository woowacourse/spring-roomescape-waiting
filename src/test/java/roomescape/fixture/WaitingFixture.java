package roomescape.fixture;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import roomescape.member.domain.Member;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Theme;
import roomescape.reservation.model.Waiting;

public class WaitingFixture {

    public static Waiting getOneWithTheme(Theme theme) {
        return new Waiting(
                null,
                MemberFixture.getOne(),
                LocalDate.parse("3000-10-10"),
                ReservationTimeFixture.getOne(),
                theme
        );
    }

    public static Waiting getOneWithMember(Member member) {
        return new Waiting(
                null,
                member,
                LocalDate.parse("3000-10-10"),
                ReservationTimeFixture.getOne(),
                ThemeFixture.getOne()
        );
    }

    public static Waiting getOneWithDateTimeTheme(LocalDate date,
                                                  ReservationTime reservationTime,
                                                  Theme theme) {
        return new Waiting(
                null,
                MemberFixture.getOne(),
                date,
                reservationTime,
                theme
        );
    }

    public static Waiting getOneWithTimeTheme(ReservationTime reservationTime,
                                              Theme theme) {
        return new Waiting(
                null,
                MemberFixture.getOne(),
                LocalDate.parse("3000-10-10"),
                reservationTime,
                theme
        );
    }

    public static Waiting getOneWithMemberTimeTheme(Member member, ReservationTime reservationTime,
                                                    Theme theme) {
        return new Waiting(
                null,
                member,
                LocalDate.parse("3000-10-10"),
                reservationTime,
                theme
        );
    }

    public static List<Waiting> get(int count) {
        List<Waiting> reservations = new ArrayList<>();
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
