package roomescape.domain.reservation.dto;

import roomescape.domain.member.dto.MemberResponse;
import roomescape.domain.reservation.domain.Reservation;
import roomescape.domain.reservation.domain.ReservationStatus;
import roomescape.domain.theme.domain.Theme;
import roomescape.domain.time.domain.ReservationTime;

import java.time.LocalDate;
import java.util.List;

public record ReservationResponse(Long id,
                                  LocalDate date,
                                  ReservationStatus status,
                                  ReservationTime time,
                                  Theme theme,
                                  MemberResponse memberResponse) {
    public static ReservationResponse from(Reservation reservation) {
        MemberResponse memberResponse = new MemberResponse(
                reservation.getMember().getId(),
                reservation.getMember().getName(),
                reservation.getMember().getEmail(),
                reservation.getMember().getRole()
        );
        return new ReservationResponse(reservation.getId(),
                reservation.getDate(),
                reservation.getStatus(),
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
