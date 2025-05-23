package roomescape.dto.reservation;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.dto.member.MemberResponse;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;

public record ReservationResponse(Long id, LocalDate date, ReservationTimeResponse time,
                                  ThemeResponse theme, MemberResponse member, String status) {

    public static ReservationResponse fromWaitingReservation(Reservation reservation, int order) {
        return mapToReservationResponse(reservation, String.format("%d번째 예약대기", order));
    }

    public static ReservationResponse fromConfirmedReservation(Reservation reservation) {
        return mapToReservationResponse(reservation, reservation.getStatus().getValue());
    }

    private static ReservationResponse mapToReservationResponse(Reservation reservation, String status) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                MemberResponse.from(reservation.getMember()),
                status
        );
    }
}
