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
        Member member,
        ReservationTime reservationTime,
        ReservationTheme reservationTheme
    ) {
        return ReservationDetails.builder()
                .member(member)
                .date(date)
                .reservationTime(reservationTime)
                .reservationTheme(reservationTheme)
                .build();
    }
}
