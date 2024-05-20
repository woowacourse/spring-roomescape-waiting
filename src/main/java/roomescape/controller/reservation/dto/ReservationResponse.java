package roomescape.controller.reservation.dto;

import java.time.format.DateTimeFormatter;
import roomescape.controller.theme.dto.ReservationThemeResponse;
import roomescape.controller.time.dto.AvailabilityTimeResponse;
import roomescape.domain.Reservation;

public record ReservationResponse(Long id,
                                  MemberResponse member,
                                  String date,
                                  AvailabilityTimeResponse time,
                                  ReservationThemeResponse theme) {

    public static ReservationResponse from(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                MemberResponse.from(reservation.getMember()),
                reservation.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                AvailabilityTimeResponse.from(reservation.getTime(), false),
                ReservationThemeResponse.from(reservation.getTheme())
        );
    }
}
