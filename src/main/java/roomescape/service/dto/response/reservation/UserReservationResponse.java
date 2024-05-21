package roomescape.service.dto.response.reservation;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaitWithRank;

public record UserReservationResponse(Long id,
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

    public UserReservationResponse(ReservationWaitWithRank reservationWaitWithRank) {
        this(reservationWaitWithRank.getReservationWait().getId(),
                reservationWaitWithRank.getReservationWait().getTheme().getName(),
                reservationWaitWithRank.getReservationWait().getDate(),
                reservationWaitWithRank.getReservationWait().getTime().getStartAt(),
                String.format("%d%s", reservationWaitWithRank.getRank() + 1, "번째 예약 대기"));
    }
}
