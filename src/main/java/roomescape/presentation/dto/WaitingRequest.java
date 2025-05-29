package roomescape.presentation.dto;

import java.time.LocalDate;
import roomescape.business.domain.Reservation;

public record WaitingRequest(LocalDate date, Long timeId, Long themeId) {
    public static WaitingRequest from(Reservation reservation) {
        return new WaitingRequest(reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
    }
}
