package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.dto.ReservationRank;

public record ReservationMineResponse(long id,
                                      String theme,
                                      LocalDate date,
                                      @JsonFormat(pattern = "HH:mm") LocalTime time,
                                      String status,
                                      long rank) {

    public static ReservationMineResponse from(ReservationRank reservationRank) {
        Reservation reservation = reservationRank.reservation();
        return new ReservationMineResponse(
                reservation.getId(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt(),
                reservation.getStatusName(),
                reservationRank.rank()
        );
    }
}
