package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record MemberReservationResponse(
        Long reservationId,
        ThemeResponse theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        ReservationStatus status) {

    public static MemberReservationResponse from(Reservation reservation) {

        return new MemberReservationResponse(
                reservation.getId(),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getDate(),
                reservation.getReservationTime().getStartAt(),
                ReservationStatus.RESERVED); // reservation.getStatus()로 받아올 수 있게 하기
    }
}
