package roomescape.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.dto.WaitingWithRankDto;

public record MyReservationResponse(
        Long id,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        String theme,
        ReservationStatus status,
        Long rank
) {
    public static MyReservationResponse from(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getTheme().getName(),
                ReservationStatus.RESERVED,
                0L
        );
    }

    public static MyReservationResponse from(WaitingWithRankDto waitingWithRankDto) {
        Waiting waiting = waitingWithRankDto.waiting();
        Long rank = waitingWithRankDto.rank();

        return new MyReservationResponse(
                waiting.getId(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                waiting.getTheme().getName(),
                ReservationStatus.WAITING,
                rank
        );
    }
}
