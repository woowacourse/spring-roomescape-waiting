package roomescape.application.dto;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.Reservation;

public record ReservationServiceResponse(
        long id,
        MemberServiceResponse member,
        ThemeServiceResponse theme,
        LocalDate date,
        TimeServiceResponse time
) {

    public static ReservationServiceResponse from(Reservation reservation) {
        return new ReservationServiceResponse(
                reservation.getId(),
                MemberServiceResponse.from(reservation.getMember()),
                ThemeServiceResponse.from(reservation.getTheme()),
                reservation.getDate(),
                TimeServiceResponse.from(reservation.getTime())
        );
    }

    public static List<ReservationServiceResponse> from(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationServiceResponse::from)
                .toList();
    }
}
