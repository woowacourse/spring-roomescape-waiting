package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record MemberReservationResponse(
        Long reservationId,
        String theme,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String status
) {

    public static MemberReservationResponse from(Reservation reservation) {
        return new MemberReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getStatus().getName());
    }
}
