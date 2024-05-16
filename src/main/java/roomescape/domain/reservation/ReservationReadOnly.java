package roomescape.domain.reservation;

import java.time.LocalDate;
import roomescape.domain.member.Member;

public record ReservationReadOnly(
        Long id,
        Member member,
        LocalDate date,
        ReservationTime time,
        Theme theme
) {
}
