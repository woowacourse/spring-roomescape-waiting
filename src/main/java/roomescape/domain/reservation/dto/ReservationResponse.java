package roomescape.domain.reservation.dto;

import java.util.List;
import roomescape.domain.member.dto.MemberResponse;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.reservation.domain.reservation.ReservationDate;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.theme.domain.Theme;

public record ReservationResponse(Long id,
                                  ReservationDate date,
                                  ReservationTime time,
                                  Theme theme,
                                  MemberResponse memberResponse) {
    public static ReservationResponse from(Reservation reservation) {
        MemberResponse memberResponse = new MemberResponse(
                reservation.getMemberId(),
                reservation.getMemberName(),
                reservation.getMemberEmail(),
                reservation.getMemberRole()
        );
        return new ReservationResponse(reservation.getId(),
                reservation.getReservationDate(),
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
