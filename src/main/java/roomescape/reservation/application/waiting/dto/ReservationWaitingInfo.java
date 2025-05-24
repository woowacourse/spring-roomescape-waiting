package roomescape.reservation.application.waiting.dto;

import roomescape.member.application.dto.MemberInfo;
import roomescape.reservation.application.reservation.dto.ReservationInfo;
import roomescape.reservation.domain.reservation.ReservationStatus;
import roomescape.reservation.domain.waiting.ReservationWaiting;
import roomescape.reservation.domain.waiting.ReservationWaitingWithRank;

public record ReservationWaitingInfo(
        long id,
        ReservationInfo reservation,
        MemberInfo member,
        String status
) {

    public ReservationWaitingInfo(final ReservationWaiting reservationWaiting) {
        this(
                reservationWaiting.id(),
                new ReservationInfo(reservationWaiting.reservation()),
                new MemberInfo(reservationWaiting.member()),
                String.format(ReservationStatus.WAITING.getDisplayName(), 0)
        );
    }

    public ReservationWaitingInfo(final ReservationWaitingWithRank reservationWaitingWithRank) {
        this(
                reservationWaitingWithRank.reservationWaiting().id(),
                new ReservationInfo(reservationWaitingWithRank.reservationWaiting().reservation()),
                new MemberInfo(reservationWaitingWithRank.reservationWaiting().member()),
                String.format(ReservationStatus.WAITING.getDisplayName(), reservationWaitingWithRank.rank())
        );
    }
}
