package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.ReservationRank;
import roomescape.domain.ReservationStatus;

public record ReservationMineResponse(long id,
                                      String theme,
                                      LocalDate date,
                                      @JsonFormat(pattern = "HH:mm") LocalTime time,
                                      String status) {

    public static ReservationMineResponse from(ReservationRank reservationRank) {
        return new ReservationMineResponse(
                reservationRank.getReservation().getId(),
                reservationRank.getReservation().getTheme().getName(),
                reservationRank.getReservation().getDate(),
                reservationRank.getReservation().getTime().getStartAt(),
                convertStatus(reservationRank)
        );
    }

    private static String convertStatus(ReservationRank reservationRank) {
        ReservationStatus reservationStatus = reservationRank.getReservation().getStatus();
        if (reservationStatus.isPending()) {
            return reservationRank.getRank() + "번째 " + reservationStatus.getName();
        }
        return reservationStatus.getName();
    }
}
