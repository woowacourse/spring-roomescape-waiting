package roomescape.reservation.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;
import roomescape.member.controller.response.MemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.waiting.domain.Waiting;
import roomescape.theme.controller.response.ThemeResponse;
import roomescape.time.controller.response.ReservationTimeResponse;

public record ReservationResponse(
        Long id,
        MemberResponse member,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                MemberResponse.from(reservation.getReserver()),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getReservationTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }

    public static ReservationResponse from(Waiting waiting) {
        return new ReservationResponse(
                waiting.getId(),
                MemberResponse.from(waiting.getReserver()),
                waiting.getDate(),
                ReservationTimeResponse.from(waiting.getReservationTime()),
                ThemeResponse.from(waiting.getTheme())
        );
    }

    public static List<ReservationResponse> from(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
