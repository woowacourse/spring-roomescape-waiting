package roomescape;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.slot.domain.Slot;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static Member member(long id) {
        return new Member(id, "member" + id, "password", Role.USER);
    }

    public static Slot slot(long id) {
        return Slot.of(
                id,
                LocalDate.of(2026, 5, 5),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "theme", "description", "thumbnail")
        );
    }

    public static Reservation reservation(Long id, Long memberId, Slot slot) {
        return Reservation.of(id, member(memberId), slot);
    }

    public static Waiting waiting(Long id, Long memberId, Long slotId) {
        return Waiting.of(id, member(memberId), slot(slotId));
    }
}
