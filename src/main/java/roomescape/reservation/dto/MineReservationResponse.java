package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.WaitingRankReservation;

public record MineReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        String status,
        Long rank
) {

    public static MineReservationResponse from(final WaitingRankReservation waitingRankReservation){
        return new MineReservationResponse(
                waitingRankReservation.getReservation().getId(),
                waitingRankReservation.getReservation().getTheme().getName(),
                waitingRankReservation.getReservation().getDate().date(),
                waitingRankReservation.getReservation().getReservationTime().getStartAt(),
                waitingRankReservation.getReservation().getReservationStatus().getMessage(),
                waitingRankReservation.getWaitingRank()
        );
    }

}
