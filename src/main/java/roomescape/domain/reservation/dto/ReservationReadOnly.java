package roomescape.domain.reservation.dto;

import java.time.LocalDate;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;

public record ReservationReadOnly(
        Long id,
        Member member,
        LocalDate date,
        ReservationTime time,
        Theme theme
) {
}
