package roomescape.reservation.dto;

import roomescape.member.dto.MemberDto;
import roomescape.reservation.model.ReservationDate;
import roomescape.reservation.model.ReservationWaitingWithOrder;

public record ReservationWaitingWithOrderDto(
        Long id,
        int order,
        ReservationDate date,
        ReservationTimeDto time,
        ThemeDto theme,
        MemberDto member
) {
    public static ReservationWaitingWithOrderDto from(ReservationWaitingWithOrder reservationWaitingWithOrder) {
        return new ReservationWaitingWithOrderDto(
                reservationWaitingWithOrder.getReservationWaiting().getId(),
                reservationWaitingWithOrder.getOrder(),
                reservationWaitingWithOrder.getReservationWaiting().getDate(),
                ReservationTimeDto.from(reservationWaitingWithOrder.getReservationWaiting().getTime()),
                ThemeDto.from(reservationWaitingWithOrder.getReservationWaiting().getTheme()),
                MemberDto.from(reservationWaitingWithOrder.getReservationWaiting().getMember())
        );
    }
}
