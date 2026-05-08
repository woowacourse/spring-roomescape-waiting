package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationResponse(
        Long id,
        String name,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate date,

        ReservationTime time,
        ReservationTheme theme
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.id(),
                reservation.username(),
                reservation.reservationDate(),
                new ReservationTime(reservation.reservationTime().id(), reservation.reservationTime().startAt()),
                new ReservationTheme(
                        reservation.reservationTheme().id(),
                        reservation.reservationTheme().name(),
                        reservation.reservationTheme().thumbnailUrl(),
                        reservation.reservationTheme().description())
        );
    }

    private record ReservationTime(
            Long id,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
            LocalTime startAt) {

    }

    private record ReservationTheme(Long id,
                                    String name,
                                    String thumbnailUrl,
                                    String description
    ) {
    }
}
