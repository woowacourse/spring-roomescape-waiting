package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import roomescape.reservation.entity.Reservation;
import roomescape.waiting.entity.Waiting;
import roomescape.waiting.entity.WaitingWithRank;

public record ReservationByMemberResponse(
        Long id,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static List<ReservationByMemberResponse> of(List<Reservation> reservations,
                                                       List<WaitingWithRank> waitingWithRanks) {
        List<ReservationByMemberResponse> responsesByReservation = reservations.stream()
                .map(ReservationByMemberResponse::from)
                .toList();

        List<ReservationByMemberResponse> responsesByWaiting = waitingWithRanks.stream()
                .map(ReservationByMemberResponse::from)
                .toList();

        List<ReservationByMemberResponse> responses = new ArrayList<>();
        responses.addAll(responsesByReservation);
        responses.addAll(responsesByWaiting);
        return responses;
    }

    public static ReservationByMemberResponse from(Reservation reservation) {
        return new ReservationByMemberResponse(
                reservation.getId(),
                reservation.getReservationSlot().getTheme().getName(),
                reservation.getReservationSlot().getDate(),
                reservation.getReservationSlot().getTime().getStartAt(),
                "예약"
        );
    }

    public static ReservationByMemberResponse from(WaitingWithRank waitingWithRank) {
        Waiting waiting = waitingWithRank.getWaiting();
        return new ReservationByMemberResponse(
                waiting.getId(),
                waiting.getReservationSlot().getTheme().getName(),
                waiting.getReservationSlot().getDate(),
                waiting.getReservationSlot().getTime().getStartAt(),
                String.format("%d번째 예약대기", waitingWithRank.getRank() + 1)
        );
    }
}
