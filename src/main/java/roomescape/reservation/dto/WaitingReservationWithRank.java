package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.WaitingReservation;

public record WaitingReservationWithRank(
        Long reservationId,
        String theme,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        Long rank
) {
    public static WaitingReservationWithRank of(final WaitingReservation waitingReservation, final Long rank) {
        return new WaitingReservationWithRank(
                waitingReservation.getId(),
                waitingReservation.getRoomEscapeInformation().getTheme().getName(),
                waitingReservation.getRoomEscapeInformation().getDate(),
                waitingReservation.getRoomEscapeInformation().getTime().getStartAt(),
                rank
        );
    }
}
