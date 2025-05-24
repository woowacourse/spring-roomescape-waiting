package roomescape.reservation.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.reservation.domain.Reservation;

public record WaitingReservationResponse(
        Long waitingId,
        String reserverName,
        String themeName,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time
) {
    public static WaitingReservationResponse from(Reservation reservation) {
        return new WaitingReservationResponse(
                reservation.getId(),
                reservation.getReserverName(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt()
        );
    }

    public static List<WaitingReservationResponse> from(List<Reservation> reservations) {
        return reservations.stream()
                .map(WaitingReservationResponse::from)
                .toList();
    }
}
