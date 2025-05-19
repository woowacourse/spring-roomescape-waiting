package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.entity.Reservation;
import roomescape.waiting.entity.Waiting;
import roomescape.waiting.entity.WaitingWithRank;

public record ReservationWaitingReadMemberResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static ReservationWaitingReadMemberResponse fromReservation(Reservation reservation) {
        return new ReservationWaitingReadMemberResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                "예약"
        );
    }

    public static ReservationWaitingReadMemberResponse fromWaitingWithRank(WaitingWithRank waitingWithRank) {
        Waiting waiting = waitingWithRank.getWaiting();
        return new ReservationWaitingReadMemberResponse(
                waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                String.format("%d번째 예약대기", waitingWithRank.getRank())
        );
    }
}
