package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.domain.Reservation;

public record MemberReservationResponse(
        Long reservationId,
        ThemeResponse theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String status
) {

    public static MemberReservationResponse from(Reservation reservation) {
        return  new MemberReservationResponse(
                reservation.getId(),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getDate(),
                reservation.getReservationTime().getStartAt(),
                "예약");
    }
}
