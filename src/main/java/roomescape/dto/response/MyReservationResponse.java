package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.entity.Reservation;

public record MyReservationResponse(Long id,
                                    String theme,
                                    LocalDate date,
                                    LocalTime time,
                                    String status,
                                    Long waitRank) {

    public static MyReservationResponse from(Reservation reservation) {

        return new MyReservationResponse(
                reservation.getId(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt(),
                reservation.getStatus().getText(),
                null);
    }

    public static MyReservationResponse of(Reservation reservation, Long waitRank) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt(),
                reservation.getStatus().getText(),
                waitRank);
    }
}
