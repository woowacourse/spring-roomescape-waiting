package roomescape.domain.reservation.dto;

import roomescape.domain.member.dto.MemberResponse;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;

public record ReservationResponse(Long id,
                                  LocalDate date,
                                  ReservationTime time,
                                  Theme theme,
                                  MemberResponse memberResponse) {
    public static ReservationResponse from(Reservation reservation) {
        MemberResponse memberResponse = new MemberResponse(
                reservation.getMember().getId(),
                reservation.getMemberName(),
                reservation.getMemberEmail(),
                reservation.getMemberRole()
        );
        return new ReservationResponse(reservation.getId(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                memberResponse);
    }

    public static List<ReservationResponse> fromList(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
