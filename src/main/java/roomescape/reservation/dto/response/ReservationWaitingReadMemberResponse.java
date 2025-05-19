package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import roomescape.reservation.entity.Reservation;
import roomescape.waiting.entity.Waiting;
import roomescape.waiting.entity.WaitingWithRank;

public record ReservationWaitingReadMemberResponse(
        Long id,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static List<ReservationWaitingReadMemberResponse> of(List<Reservation> reservations,
                                                                List<WaitingWithRank> waitingWithRanks) {
        List<ReservationWaitingReadMemberResponse> responsesByReservation = reservations.stream()
                .map(ReservationWaitingReadMemberResponse::from)
                .toList();

        List<ReservationWaitingReadMemberResponse> responsesByWaiting = waitingWithRanks.stream()
                .map(ReservationWaitingReadMemberResponse::from)
                .toList();

        List<ReservationWaitingReadMemberResponse> responses = new ArrayList<>();
        responses.addAll(responsesByReservation);
        responses.addAll(responsesByWaiting);
        return responses;
    }

    public static ReservationWaitingReadMemberResponse from(Reservation reservation) {
        return new ReservationWaitingReadMemberResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                "예약"
        );
    }

    public static ReservationWaitingReadMemberResponse from(WaitingWithRank waitingWithRank) {
        Waiting waiting = waitingWithRank.getWaiting();
        return new ReservationWaitingReadMemberResponse(
                waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                String.format("%d번째 예약대기", waitingWithRank.getRank() + 1)
        );
    }
}
