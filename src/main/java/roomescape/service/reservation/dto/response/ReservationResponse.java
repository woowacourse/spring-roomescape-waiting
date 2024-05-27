package roomescape.service.reservation.dto.response;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;
import roomescape.service.member.dto.MemberResponse;

public record ReservationResponse(Long id, String name,
                                  LocalDate date, MemberResponse member,
                                  ReservationTimeResponse time, ThemeResponse theme) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getMemberName(),
                reservation.getDate(),
                MemberResponse.from(reservation.getMember()),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}
