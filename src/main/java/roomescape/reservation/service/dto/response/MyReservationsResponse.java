package roomescape.reservation.service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.waiting.repository.dto.WaitingInfoDataResponse;
import roomescape.waiting.domain.Waiting;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationsResponse(
        Long id,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String status,
        Long rank
) {

    public static MyReservationsResponse from(Reservation reservation) {
        return new MyReservationsResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                ReservationStatus.CONFIRMED.getDescription(),
                null
        );
    }

    public static MyReservationsResponse from(WaitingInfoDataResponse waitingInfoDataResponse) {
        Waiting waiting = waitingInfoDataResponse.waiting();
        return new MyReservationsResponse(
                waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                ReservationStatus.WAITING.getDescription(),
                waitingInfoDataResponse.rank().value()
        );
    }
}
