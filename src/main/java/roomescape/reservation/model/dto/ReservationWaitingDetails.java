package roomescape.reservation.model.dto;

import java.time.LocalDate;
import lombok.Builder;
import roomescape.member.model.Member;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.entity.ReservationWaiting;

@Builder
public record ReservationWaitingDetails(
        LocalDate date,
        ReservationTime reservationTime,
        ReservationTheme reservationTheme,
        Member member
) {

    public ReservationWaiting toReservationWaiting() {
        return ReservationWaiting.builder()
                .member(member)
                .date(date)
                .time(reservationTime)
                .theme(reservationTheme)
                .build();
    }
}
