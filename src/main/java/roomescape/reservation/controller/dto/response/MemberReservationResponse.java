package roomescape.reservation.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record MemberReservationResponse(
        LocalDate date,
        @JsonFormat(pattern = "kk:mm")
        LocalTime startAt,
        String themeName,
        String status
) {

    public static MemberReservationResponse from(final Reservation reservation) {
        return new MemberReservationResponse(
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getTheme().getThemeNameValue(),
                reservation.getStatus().getValue()
        );
    }
}
