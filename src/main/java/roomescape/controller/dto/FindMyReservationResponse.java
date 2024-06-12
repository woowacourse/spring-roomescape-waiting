package roomescape.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.ReservationWithRank;

public record FindMyReservationResponse(Long id,
                                        String theme,
                                        LocalDate date,
                                        @JsonFormat(pattern = "HH:mm") LocalTime time,
                                        String status,
                                        int rank) {

    public static FindMyReservationResponse from(ReservationWithRank reservationWithRank) {
        return new FindMyReservationResponse(
            reservationWithRank.getId(),
            reservationWithRank.getTheme().getName(),
            reservationWithRank.getDate().getValue(),
            reservationWithRank.getTime().getStartAt(),
            reservationWithRank.getStatus().toString(),
            reservationWithRank.getRank()
        );
    }
}
