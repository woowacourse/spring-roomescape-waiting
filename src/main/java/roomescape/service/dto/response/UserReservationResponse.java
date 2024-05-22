package roomescape.service.dto.response;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.service.dto.ReservationStatusMessageMapper;

import java.time.LocalDate;
import java.time.LocalTime;

public record UserReservationResponse(
        long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static UserReservationResponse of(Reservation reservation, ReservationStatus status, int rank) {
        return new UserReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                ReservationStatusMessageMapper.messageOf(status, rank)
        );
    }
}
