package roomescape.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.Waiting;
import roomescape.domain.dto.WaitingWithRank;

public record MyReservationResponse(
        Long id,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {

    public static MyReservationResponse convert(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                "예약"
        );
    }

    public static MyReservationResponse convert(WaitingWithRank waitingWithRank) {
        Waiting waiting = waitingWithRank.waiting();
        Long rank = waitingWithRank.rank();
        return new MyReservationResponse(
                waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                String.format("%d번째 예약대기", rank + 1)
        );
    }
}
