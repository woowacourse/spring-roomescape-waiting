package roomescape.dto.reservation;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.Reservation;
import roomescape.dto.member.MemberResponse;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;

public record ReservationResponse(Long id, LocalDate date, ReservationTimeResponse time,
                                  ThemeResponse theme, MemberResponse member) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(reservation.getId(), reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()), ThemeResponse.from(reservation.getTheme()),
                MemberResponse.from(reservation.getMember()));
    }

    public static List<ReservationResponse> from(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
