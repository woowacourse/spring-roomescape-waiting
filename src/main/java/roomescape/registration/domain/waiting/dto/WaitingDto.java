package roomescape.registration.domain.waiting.dto;

import java.time.LocalDate;
import roomescape.registration.domain.reservation.domain.Reservation;
import roomescape.registration.domain.waiting.domain.Waiting;

public record WaitingDto(LocalDate date, long themeId, long timeId, long memberId) {

    public static WaitingDto from(Waiting waiting) {
        Reservation reservation = waiting.getReservation();

        return new WaitingDto(reservation.getDate(), reservation.getTheme().getId(),
                reservation.getReservationTime().getId(), reservation.getMember().getId());
    }
}
