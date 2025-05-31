package roomescape.dto.reservation;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.waiting.ReservationWaitingRank;

public record MyReservationResponseDto(
        Long id,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        String statusMessage
) {

    public MyReservationResponseDto(Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getStatus().getMessage()
        );
    }

    public MyReservationResponseDto(Reservation reservation, ReservationWaitingRank rank) {
        this(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                rank.getStatusMessage()
        );
    }
}
