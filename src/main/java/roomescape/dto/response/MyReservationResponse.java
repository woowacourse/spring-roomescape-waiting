package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.ReservationWithRank;
import roomescape.entity.Reservation;

public record MyReservationResponse(Long id,
                                    String theme,
                                    LocalDate date,
                                    LocalTime time,
                                    String status) {

    public static MyReservationResponse from(ReservationWithRank reservationWithRank) {

        Reservation reservation = reservationWithRank.getReservation();

        return new MyReservationResponse(
                reservation.getId(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt(),
                reservation.getStatus().renderText(reservationWithRank.getRank()));
    }
}
