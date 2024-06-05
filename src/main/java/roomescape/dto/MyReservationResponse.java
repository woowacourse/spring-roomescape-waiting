package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;

public record MyReservationResponse(
        long id,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        String themeName,
        String status
) {
    public static MyReservationResponse from(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                reservation.getStartAt(),
                reservation.getThemeName(),
                "예약"
        );
    }

    public static MyReservationResponse of(ReservationWaiting waiting, int rank) {
        return new MyReservationResponse(
                waiting.getId(),
                waiting.getDate(),
                waiting.getStartAt(),
                waiting.getThemeName(),
                "%d번째 예약 대기".formatted(rank)
        );
    }
}
