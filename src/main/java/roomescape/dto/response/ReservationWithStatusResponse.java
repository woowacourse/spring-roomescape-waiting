package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.Status;
import roomescape.domain.WaitingWithRank;

public record ReservationWithStatusResponse(
        Long id,
        String memberName,
        LocalDate date,
        ReservationTimeResponse time,
        String themeName,
        String status
) {
    public static ReservationWithStatusResponse from(Reservation reservation) {
        ReservationTimeResponse dto = ReservationTimeResponse.from(reservation.getReservationTime());
        return new ReservationWithStatusResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getDate(),
                dto,
                reservation.getTheme().getName(),
                Status.CONFIRMED.toString()
        );
    }

    public static ReservationWithStatusResponse from(WaitingWithRank waitingWithRank) {
        ReservationTimeResponse dto = ReservationTimeResponse.from(waitingWithRank.waiting().getReservationTime());
        return new ReservationWithStatusResponse(
                waitingWithRank.waiting().getId(),
                waitingWithRank.waiting().getMember().getName(),
                waitingWithRank.waiting().getDate(),
                dto,
                waitingWithRank.waiting().getTheme().getName(),
                String.format("%d번째 예약대기", waitingWithRank.rank())
        );
    }
}
