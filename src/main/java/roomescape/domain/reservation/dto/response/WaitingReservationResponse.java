package roomescape.domain.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.domain.reservation.domain.reservation.Reservation;

public record WaitingReservationResponse(Long id, String name, String theme, LocalDate date, LocalTime time) {
    public static List<WaitingReservationResponse> fromList(List<Reservation> reservations) {
        return reservations.stream()
                .map(reservation -> new WaitingReservationResponse(reservation.getId(),
                        reservation.getMember().getName(), reservation.getTheme().getName(),
                        reservation.getDate(), reservation.getTime().getStartAt()))
                .toList();
    }
}
