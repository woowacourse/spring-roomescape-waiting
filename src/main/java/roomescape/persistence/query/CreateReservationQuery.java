package roomescape.persistence.query;

import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;

public record CreateReservationQuery(
        Member member,
        LocalDate date,
        ReservationTime time,
        Theme theme
) {
}
