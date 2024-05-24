package roomescape.reservation.dto.response;

import java.time.format.DateTimeFormatter;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.WaitingWithRank;

public record ReservationOrWaitingResponse(
        Long id,
        String themeName,
        String date,
        String startAt,
        String status
) {

    public ReservationOrWaitingResponse(Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getTheme().getName().name(),
                reservation.getDate(DateTimeFormatter.ISO_DATE),
                reservation.getReservationTime().getStartAt(DateTimeFormatter.ofPattern("HH:mm")),
                "예약"
        );
    }

    public ReservationOrWaitingResponse(WaitingWithRank waitingWithRank) {
        this(
                waitingWithRank.waiting().getId(),
                waitingWithRank.waiting().getTheme().getName().name(),
                waitingWithRank.waiting().getDate(DateTimeFormatter.ISO_DATE),
                waitingWithRank.waiting().getReservationTime().getStartAt(DateTimeFormatter.ofPattern("HH:mm")),
                waitingWithRank.rank() + "번째 예약대기"
        );
    }
}
