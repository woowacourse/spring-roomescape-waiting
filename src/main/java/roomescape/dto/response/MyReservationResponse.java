package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.Reservation;

import java.time.LocalDate;

public record MyReservationResponse(
        Long reservationId,
        ThemeResponse theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") ReservationTimeResponse time,
        WaitingResponse waiting) {

    public static MyReservationResponse of(Reservation reservation, Long waitingRank) {
        return new MyReservationResponse(
                reservation.getId(),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getReservationTime()),
                new WaitingResponse(reservation.getStatus(), waitingRank));
    }
}
