package roomescape.service.dto.response;

import java.time.LocalDate;
import roomescape.domain.Reservation;

public record ReservationResponse(Long id, String name,
                                  LocalDate date, MemberResponse member,
                                  ReservationTimeResponse time, ThemeResponse theme) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getSchedule().getDate(),
                MemberResponse.from(reservation.getMember()),
                ReservationTimeResponse.from(reservation.getSchedule().getTime()),
                ThemeResponse.from(reservation.getSchedule().getTheme())
        );
    }
}
