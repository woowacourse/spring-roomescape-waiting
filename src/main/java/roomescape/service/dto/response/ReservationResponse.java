package roomescape.service.dto.response;

import java.time.LocalDate;
import roomescape.entity.Reservation;

public record ReservationResponse(Long id, String name,
                                  LocalDate date, MemberResponse member,
                                  ReservationTimeResponse time, ThemeResponse theme) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getDate(),
                MemberResponse.from(reservation.getMember()),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}
