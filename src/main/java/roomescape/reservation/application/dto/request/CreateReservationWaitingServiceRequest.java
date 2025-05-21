package roomescape.reservation.application.dto.request;

import java.time.LocalDate;
import roomescape.member.model.Member;
import roomescape.reservation.model.dto.ReservationWaitingDetails;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;

public record CreateReservationWaitingServiceRequest(
        Long memberId,
        LocalDate date,
        Long timeId,
        Long themeId
) {

    public ReservationWaitingDetails toReservationWaitingDetails(
            ReservationTime reservationTime,
            ReservationTheme reservationTheme,
            Member member
    ) {
        return ReservationWaitingDetails.builder()
                .date(date)
                .reservationTime(reservationTime)
                .reservationTheme(reservationTheme)
                .member(member)
                .build();
    }
}
