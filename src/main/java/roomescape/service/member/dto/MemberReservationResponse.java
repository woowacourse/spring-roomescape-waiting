package roomescape.service.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationWaiting;
import roomescape.domain.reservation.WaitingWithRank;

public record MemberReservationResponse(
        Long reservationId,
        String theme,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul") LocalDate date,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul") LocalTime time,
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
        ReservationWaiting waiting = waitingWithRank.getWaiting();
        return new MemberReservationResponse(
                waiting.getId(),
                waiting.getTheme().getName().getValue(),
                waiting.getDate(),
                waiting.getSchedule().getTime(),
                waitingWithRank.getRank() + "번째 예약대기"
        );
    }
}
