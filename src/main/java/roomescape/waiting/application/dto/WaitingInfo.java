package roomescape.waiting.application.dto;

import roomescape.member.application.dto.MemberInfo;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.presentation.util.ReservationStatusDisplay;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

public record WaitingInfo(
        long id,
        ReservationInfo reservationInfo,
        MemberInfo member,
        String status
) {

    public WaitingInfo(final Waiting waiting) {
        this(
                waiting.id(),
                new ReservationInfo(waiting.reservation()),
                new MemberInfo(waiting.member()),
                ReservationStatusDisplay.display(ReservationStatus.WAITING, 0)
        );
    }

    public WaitingInfo(final WaitingWithRank waitingWithRank) {
        this(
                waitingWithRank.waiting().id(),
                new ReservationInfo(waitingWithRank.waiting().reservation()),
                new MemberInfo(waitingWithRank.waiting().member()),
                ReservationStatusDisplay.display(ReservationStatus.WAITING, waitingWithRank.rank())
        );
    }
}
