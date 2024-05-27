package roomescape.registration.dto;

import java.time.LocalDate;
import roomescape.registration.domain.reservation.domain.Reservation;
import roomescape.registration.domain.waiting.domain.Waiting;

public record RegistrationRequest(LocalDate date, long themeId, long timeId, long memberId) {

    public static RegistrationRequest from(Waiting waiting) {
        Reservation reservation = waiting.getReservation();

        return new RegistrationRequest(reservation.getDate(), reservation.getTheme().getId(),
                reservation.getReservationTime().getId(), reservation.getMember().getId());
    }
}
