package roomescape.service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;

public record UserReservationResponse(
        long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
        public static UserReservationResponse of(Reservation reservation, ReservationStatus status) {
                return new UserReservationResponse(
                        reservation.getId(),
                        reservation.getTheme().getName(),
                        reservation.getDate(),
                        reservation.getTime().getStartAt(),
                        status.getValue()
                );
        }
}
