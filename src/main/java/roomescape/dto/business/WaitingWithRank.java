package roomescape.dto.business;

import java.time.LocalDate;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

public record WaitingWithRank(
        Long id,
        LocalDate date,
        Theme theme,
        ReservationTime time,
        Member member,
        Long rank
) {

    public WaitingWithRank(Waiting waiting, Long rank) {
        this(waiting.getId(), waiting.getDate(), waiting.getTheme(), waiting.getTime(), waiting.getMember(), rank);
    }
}
