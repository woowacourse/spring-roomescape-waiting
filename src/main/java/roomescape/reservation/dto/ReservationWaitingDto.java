package roomescape.reservation.dto;

import roomescape.member.dto.MemberDto;
import roomescape.reservation.model.ReservationDate;
import roomescape.reservation.model.ReservationWaiting;

public record ReservationWaitingDto(
        Long id,
        ReservationDate date,
        ReservationTimeDto time,
        ThemeDto theme,
        MemberDto member
) {
    public static ReservationWaitingDto from(ReservationWaiting reservationWaiting) {
        return new ReservationWaitingDto(
                reservationWaiting.getId(),
                reservationWaiting.getDate(),
                ReservationTimeDto.from(reservationWaiting.getTime()),
                ThemeDto.from(reservationWaiting.getTheme()),
                MemberDto.from(reservationWaiting.getMember())
        );
    }
}
