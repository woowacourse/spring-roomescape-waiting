package roomescape.dto.response;

import roomescape.domain.Reservation;
import roomescape.dto.result.WaitingResponseResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record WaitingResponse(
        Long order,
        WaitingReservationResponse reservation,
        Long memberId,
        LocalDateTime createdAt
) {
    public static record WaitingReservationResponse(
            Long id,
            LocalDate date,
            ReservationTimeResponse time,
            ThemeResponse theme,
            StoreResponse store
    ) {
    }

    public static WaitingResponse from(WaitingResponseResult waitingResponseResult) {
        Reservation reservation = waitingResponseResult.reservation();
        WaitingReservationResponse reservationResponse = new WaitingReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(waitingResponseResult.theme()),
                StoreResponse.from(waitingResponseResult.store())
        );
        return new WaitingResponse(
                waitingResponseResult.order(),
                reservationResponse,
                waitingResponseResult.memberId(),
                waitingResponseResult.createdAt()
        );
    }

    public static List<WaitingResponse> fromAll(List<WaitingResponseResult> waitingResponseResults) {
        return waitingResponseResults.stream()
                .map(WaitingResponse::from)
                .toList();
    }
}
