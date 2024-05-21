package roomescape.service.dto.response.reservation;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWait;

public record UserReservationResponse(Long reservationId,
                                      String theme,
                                      @JsonFormat(pattern = "YYYY-MM-dd") LocalDate date,
                                      @JsonFormat(pattern = "HH:mm") LocalTime time,
                                      String status) {

    public UserReservationResponse(Reservation reservation) {
        this(reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getReservationTime().getStartAt(),
                "예약");
    }

    public UserReservationResponse(ReservationWait reservationWait) {
        this(reservationWait.getId(),
                reservationWait.getTheme().getName(),
                reservationWait.getDate(),
                reservationWait.getTime().getStartAt(),
                "예약 대기");
    }
}
