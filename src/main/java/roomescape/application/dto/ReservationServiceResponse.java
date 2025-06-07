package roomescape.application.dto;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.entity.Reservation;

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
                ThemeServiceResponse.from(reservation.getGameSchedule().getTheme()),
                reservation.getGameSchedule().getDate(),
                TimeServiceResponse.from(reservation.getGameSchedule().getTime())
        );
    }

    public static List<ReservationServiceResponse> from(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationServiceResponse::from)
                .toList();
    }
}
