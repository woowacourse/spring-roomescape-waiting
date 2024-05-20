package roomescape.service.dto.response.reservation;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.Status;
import roomescape.service.dto.response.member.MemberResponse;
import roomescape.service.dto.response.theme.ThemeResponse;
import roomescape.service.dto.response.time.ReservationTimeResponse;

public record ReservationResponse(
        Long id,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        MemberResponse member,
        Status status
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getDetail().getDate(),
                ReservationTimeResponse.from(reservation.getDetail().getTime()),
                ThemeResponse.from(reservation.getDetail().getTheme()),
                MemberResponse.from(reservation.getMember()),
                reservation.getStatus()
        );
    }
}
