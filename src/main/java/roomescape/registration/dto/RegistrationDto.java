package roomescape.registration.dto;

import java.time.LocalDate;
import roomescape.registration.domain.reservation.domain.Reservation;
import roomescape.registration.domain.waiting.domain.Waiting;

public record RegistrationDto(LocalDate date, long themeId, long timeId, long memberId) {

    public static RegistrationDto from(Waiting waiting) {
        Reservation reservation = waiting.getReservation();

        return new RegistrationDto(reservation.getDate(), reservation.getTheme().getId(),
                reservation.getReservationTime().getId(), reservation.getMember().getId());
    }
}
