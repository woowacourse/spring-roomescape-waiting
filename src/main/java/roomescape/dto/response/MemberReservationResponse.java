package roomescape.dto.response;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.domain.Reservation;

public record MemberReservationResponse(
        Long reservationId,
        ThemeResponse theme,
        String date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        ReservationWaitingResponse waiting
) {

    public static MemberReservationResponse from(Reservation reservation) {
        return new MemberReservationResponse(
                reservation.getId(),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getDate().toString(),
                reservation.getReservationTime().getStartAt(),
                null
        );
    }

    public static MemberReservationResponse of(
            Reservation reservation,
            ReservationWaitingResponse waiting
    ) {
        return new MemberReservationResponse(
                reservation.getId(),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getDate().toString(),
                reservation.getReservationTime().getStartAt(),
                waiting
        );
    }
}
