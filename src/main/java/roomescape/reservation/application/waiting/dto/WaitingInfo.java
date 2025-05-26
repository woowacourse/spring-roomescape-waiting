package roomescape.reservation.application.waiting.dto;

import java.time.LocalDate;
import roomescape.member.application.dto.MemberInfo;
import roomescape.reservation.application.theme.dto.ThemeInfo;
import roomescape.reservation.application.timeslot.dto.TimeSlotInfo;
import roomescape.reservation.domain.reservation.ReservationStatus;
import roomescape.reservation.domain.waiting.Waiting;
import roomescape.reservation.domain.waiting.WaitingWithRank;
import roomescape.reservation.ui.reservation.display.ReservationStatusDisplay;

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
