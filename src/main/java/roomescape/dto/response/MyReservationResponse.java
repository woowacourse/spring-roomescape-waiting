package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.dto.ReservationWithRankDto;

public record MyReservationResponse(
        Long id,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        String theme,
        ReservationStatus status,
        Long rank
) {

    public static MyReservationResponse from(ReservationWithRankDto reservationWithRankDto) {
        Reservation reservation = reservationWithRankDto.reservation();
        Long rank = reservationWithRankDto.rank();

        return new MyReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getTheme().getName(),
                reservation.getStatus(),
                rank
        );
    }
}
