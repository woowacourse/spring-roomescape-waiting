package roomescape.reservation.dto;

import java.time.LocalDate;
import roomescape.exception.custom.reason.ResponseInvalidException;
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.Reservation;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.theme.dto.ThemeResponse;

public record ReservationResponse(
        Long id,
        LocalDate date,
        MemberResponse member,
        ReservationTimeResponse time,
        ThemeResponse theme
) {
    public ReservationResponse {
        if (id == null || date == null || time == null || theme == null) {
            throw new ResponseInvalidException();
        }
    }

    public static ReservationResponse from(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                MemberResponse.from(reservation.getMember()),
                ReservationTimeResponse.from(reservation.getReservationTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}
