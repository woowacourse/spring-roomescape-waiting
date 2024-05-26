package roomescape.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.Waiting;
import roomescape.domain.dto.WaitingWithRankDto;

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

    public static MyReservationResponse convert(WaitingWithRankDto waitingWithRankDto) {
        Waiting waiting = waitingWithRankDto.waiting();
        Long rank = waitingWithRankDto.rank();
        return new MyReservationResponse(
                waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                String.format("%d번째 예약대기", rank + 1)
        );
    }
}
