package roomescape.reservation.presentation.dto;

import java.time.LocalDate;
import java.util.List;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.reservation.domain.Reservation;

public record AdminWaitingReservationResponse(Long id, LocalDate date, ReservationTimeResponse time,
                                              ThemeResponse theme, MemberResponse member) {

    public static AdminWaitingReservationResponse from(Reservation reservation) {
        return new AdminWaitingReservationResponse(reservation.getId(), reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()), ThemeResponse.from(reservation.getTheme()),
                MemberResponse.from(reservation.getMember())
        );
    }

    public static List<AdminWaitingReservationResponse> from(List<Reservation> reservations) {
        return reservations.stream()
                .map(AdminWaitingReservationResponse::from)
                .toList();
    }
}
