package roomescape.application.reservation.dto.response;

import java.time.LocalDate;
import roomescape.application.member.dto.response.MemberResponse;
import roomescape.domain.reservation.Reservation;

public record ReservationResponse(long id,
                                  MemberResponse member,
                                  LocalDate date,
                                  ReservationTimeResponse time,
                                  ThemeResponse theme) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                MemberResponse.from(reservation.getMember()),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}
