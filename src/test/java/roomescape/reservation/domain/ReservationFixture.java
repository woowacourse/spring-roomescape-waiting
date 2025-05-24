package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.member.domain.MemberRole;
import roomescape.member.fixture.MemberFixture;
import roomescape.reservationtime.domain.ReservationTimeFixture;
import roomescape.theme.domain.ThemeFixture;

public class ReservationFixture {
    private static final AtomicLong identifier = new AtomicLong(1L);

    public static Reservation create() {
        long id = identifier.getAndIncrement();
        return new Reservation(
            id,
            LocalDate.now(),
            ReservationTimeFixture.create(),
            ThemeFixture.create(),
            MemberFixture.create(MemberRole.USER),
            (int) id
        );
    }

    public static Reservation createWithoutId() {
        long id = identifier.getAndIncrement();
        return new Reservation(
            LocalDate.now(),
            ReservationTimeFixture.create(),
            ThemeFixture.create(),
            MemberFixture.createWithoutId(MemberRole.USER),
            (int) id
        );
    }
}
