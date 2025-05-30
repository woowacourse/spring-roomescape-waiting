package roomescape.waiting.application.dto;

import java.time.LocalDate;
import roomescape.member.application.dto.MemberInfo;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.presentation.util.ReservationStatusDisplay;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.timeslot.application.dto.TimeSlotInfo;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

public record WaitingInfo(
        long id,
        LocalDate date,
        TimeSlotInfo time,
        ThemeInfo theme,
        MemberInfo member,
        String status
) {

    public WaitingInfo(final Waiting waiting) {
        this(
                waiting.id(),
                waiting.date(),
                new TimeSlotInfo(waiting.time()),
                new ThemeInfo(waiting.theme()),
                new MemberInfo(waiting.member()),
                ReservationStatusDisplay.display(ReservationStatus.WAITING, 0)
        );
    }

    public WaitingInfo(final WaitingWithRank waitingWithRank) {
        this(
                waitingWithRank.waiting().id(),
                waitingWithRank.waiting().date(),
                new TimeSlotInfo(waitingWithRank.waiting().time()),
                new ThemeInfo(waitingWithRank.waiting().theme()),
                new MemberInfo(waitingWithRank.waiting().member()),
                ReservationStatusDisplay.display(ReservationStatus.WAITING, waitingWithRank.rank())
        );
    }
}
