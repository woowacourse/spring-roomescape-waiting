package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;
import roomescape.reservation.domain.Reservation;

public record MyReservationResponse(
        Long reservationId,
        String theme,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        String status
) {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("reservationStatus");

    public MyReservationResponse(final Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                RESOURCE_BUNDLE.getString(reservation.getStatus().name())
        );
    }
}
