package roomescape.controller.reservation.dto;

import java.time.format.DateTimeFormatter;
import roomescape.controller.theme.dto.ReservationThemeResponse;
import roomescape.controller.time.dto.ReadTimeResponse;
import roomescape.domain.Reservation;

public record ReservationResponse(Long id,
                                  MemberResponse member,
                                  String date,
                                  ReadTimeResponse time,
                                  ReservationThemeResponse theme) {

    public static ReservationResponse from(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                MemberResponse.from(reservation.getMember()),
                reservation.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                ReadTimeResponse.from(reservation.getTime()),
                ReservationThemeResponse.from(reservation.getTheme())
        );
    }
}
