package roomescape.reservation.controller.response;

import java.time.LocalDate;
import java.util.List;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.controller.response.ThemeResponse;
import roomescape.time.controller.response.ReservationTimeResponse;
import roomescape.waiting.domain.Waiting;

public record ReservationResponse(
        Long id,
        Member member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {
    public static ReservationResponse fromReservation(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getMember(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getReservationTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }

    public static List<ReservationResponse> fromReservation(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationResponse::fromReservation)
                .toList();
    }

    public static ReservationResponse fromWaiting(Waiting waiting) {
        return new ReservationResponse(
                waiting.getId(),
                waiting.getMember(),
                waiting.getDate(),
                ReservationTimeResponse.from(waiting.getReservationTime()),
                ThemeResponse.from(waiting.getTheme())
        );
    }
}
