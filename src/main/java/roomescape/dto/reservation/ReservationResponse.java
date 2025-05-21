package roomescape.dto.reservation;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.dto.member.MemberResponse;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;

public record ReservationResponse(Long id, LocalDate date, ReservationTimeResponse time,
                                  ThemeResponse theme, MemberResponse member, String status) {

    public static ReservationResponse from(Reservation reservation) {
        ReservationStatus status = reservation.getStatus();
        String statusMessage = status.getStatus().getValue();

        if (status.getStatus().isWaiting()) {
            statusMessage = String.format("%d번째 예약대기", status.getPriority());
        }
        return new ReservationResponse(reservation.getId(), reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()), ThemeResponse.from(reservation.getTheme()),
                MemberResponse.from(reservation.getMember()),
                statusMessage);
    }

    public static List<ReservationResponse> from(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
