package roomescape.reservation.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;

public record MyHistoryResponse(
        Long id,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String status
) {

    public static MyHistoryResponse ofReservation(Reservation reservation) {
        return new MyHistoryResponse(
                reservation.getId(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt(),
                "예약"
        );
    }

    public static MyHistoryResponse ofWaiting(Waiting waiting, Long count) {
        return new MyHistoryResponse(
                waiting.getId(),
                waiting.getThemeName(),
                waiting.getDate(),
                waiting.getStartAt(),
                String.format("%d번째 예약대기", count + 1)
        );
    }
}
