package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.member.dto.response.MemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.Waiting;
import roomescape.reservationtime.dto.response.ReservationTimeResponse;
import roomescape.theme.dto.response.ThemeResponse;

public record ReservationResponse(
        Long id,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String reservedStatus
) {
    public static ReservationResponse of(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                MemberResponse.from(reservation.getMember()),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()), ThemeResponse.from(reservation.getTheme()),
                ReservationStatus.WAITING.getName()
        );
    }

    public static ReservationResponse of(Waiting waiting) {
        return new ReservationResponse(
                waiting.getId(),
                MemberResponse.from(waiting.getMember()),
                waiting.getDate(),
                ReservationTimeResponse.from(waiting.getTime()), ThemeResponse.from(waiting.getTheme()),
                ReservationStatus.WAITING.getName()
        );
    }
}
