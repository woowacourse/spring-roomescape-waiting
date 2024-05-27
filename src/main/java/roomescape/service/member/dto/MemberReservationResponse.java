package roomescape.service.member.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.config.DateFormatConstraint;
import roomescape.config.TimeFormatConstraint;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationWaiting;
import roomescape.domain.reservation.WaitingWithRank;

public record MemberReservationResponse(
        Long reservationId,
        String theme,
        @DateFormatConstraint LocalDate date,
        @TimeFormatConstraint LocalTime time,
        String status
) {
    public static MemberReservationResponse from(Reservation reservation) {
        return new MemberReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName().getValue(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getStatus().getDescription()
        );
    }

    public static MemberReservationResponse from(WaitingWithRank waitingWithRank) {
        ReservationWaiting waiting = waitingWithRank.waiting();
        return new MemberReservationResponse(
                waiting.getId(),
                waiting.getTheme().getName().getValue(),
                waiting.getDate(),
                waiting.getSchedule().getTime(),
                waitingWithRank.rank() + "번째 예약대기"
        );
    }
}
