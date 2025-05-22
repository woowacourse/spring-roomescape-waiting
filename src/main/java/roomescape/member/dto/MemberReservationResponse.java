package roomescape.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

import roomescape.reservation.reservation.Reservation;

public record MemberReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        String status
) {

    public static MemberReservationResponse of(Reservation reservation) {
        return new MemberReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getReservationTime().getStartAt(),
                reservation.getReservationStatus().getMessage()
        );
    }
}
