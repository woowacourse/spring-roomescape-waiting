package roomescape.waiting.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.waiting.domain.Waiting;

public record WaitingDto(LocalDate date, long themeId, long timeId, long memberId) {

    public static WaitingDto from(Waiting waiting) {
        Reservation reservation = waiting.getReservation();

        return new WaitingDto(reservation.getDate(), reservation.getTheme().getId(),
                reservation.getReservationTime().getId(), reservation.getMember().getId());
    }
}
