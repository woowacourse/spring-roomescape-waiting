package roomescape.reservation.application.waiting.dto;

import java.time.LocalDate;
import roomescape.member.application.dto.MemberInfo;
import roomescape.reservation.application.theme.dto.ThemeInfo;
import roomescape.reservation.application.time.dto.ReservationTimeInfo;
import roomescape.reservation.domain.reservation.ReservationStatus;
import roomescape.reservation.domain.waiting.ReservationWaiting;
import roomescape.reservation.domain.waiting.ReservationWaitingWithRank;

public record ReservationWaitingInfo(
        long id,
        LocalDate date,
        ReservationTimeInfo time,
        ThemeInfo theme,
        MemberInfo member,
        String status
) {

    public ReservationWaitingInfo(final ReservationWaiting reservationWaiting) {
        this(
                reservationWaiting.id(),
                reservationWaiting.date(),
                new ReservationTimeInfo(reservationWaiting.time()),
                new ThemeInfo(reservationWaiting.theme()),
                new MemberInfo(reservationWaiting.member()),
                String.format(ReservationStatus.WAITING.getDisplayName(), 0)
        );
    }

    public ReservationWaitingInfo(final ReservationWaitingWithRank reservationWaitingWithRank) {
        this(
                reservationWaitingWithRank.reservationWaiting().id(),
                reservationWaitingWithRank.reservationWaiting().date(),
                new ReservationTimeInfo(reservationWaitingWithRank.reservationWaiting().time()),
                new ThemeInfo(reservationWaitingWithRank.reservationWaiting().theme()),
                new MemberInfo(reservationWaitingWithRank.reservationWaiting().member()),
                String.format(ReservationStatus.WAITING.getDisplayName(), reservationWaitingWithRank.rank())
        );
    }
}
