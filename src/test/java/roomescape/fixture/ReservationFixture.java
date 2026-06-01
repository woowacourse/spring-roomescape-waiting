package roomescape.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Slot;
import roomescape.domain.Theme;

public final class ReservationFixture {

    private ReservationFixture() {
    }

    public static Member member(
            String name
    ) {
        return new Member(name);
    }

    public static Slot slot(
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        return new Slot(date, time, theme);
    }

    public static Reservation reservation(
            String name,
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        return new Reservation(member(name), slot(date, time, theme));
    }

    public static Reservation reservation(
            Long id,
            String name,
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        return new Reservation(id, member(name), slot(date, time, theme));
    }

    public static Reservation reservation(
            Long id,
            String name,
            Slot slot
    ) {
        return new Reservation(id, member(name), slot);
    }

    public static ReservationWaiting waiting(
            Long id,
            String name,
            Slot slot,
            LocalDateTime createdAt
    ) {
        return new ReservationWaiting(id, member(name), slot, createdAt);
    }

    public static ReservationWaiting waiting(
            String name,
            Slot slot,
            LocalDateTime createdAt
    ) {
        return waiting(null, name, slot, createdAt);
    }
}
