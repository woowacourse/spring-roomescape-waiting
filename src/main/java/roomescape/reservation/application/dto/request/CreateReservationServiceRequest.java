package roomescape.reservation.application.dto.request;

import java.time.LocalDate;
import roomescape.member.model.Member;
import roomescape.reservation.model.dto.ReservationDetails;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;

public record CreateReservationServiceRequest(
        Long memberId,
        LocalDate date,
        Long timeId,
        Long themeId
) {

    public ReservationDetails toReservationDetails(
            ReservationTime reservationTime,
            ReservationTheme reservationTheme,
            Member member
    ) {
        return ReservationDetails.builder()
                .date(date)
                .reservationTime(reservationTime)
                .reservationTheme(reservationTheme)
                .member(member)
                .build();
    }
}
