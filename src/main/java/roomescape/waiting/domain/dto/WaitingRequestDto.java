package roomescape.waiting.domain.dto;

import java.time.LocalDate;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.user.domain.User;
import roomescape.waiting.domain.Waiting;

public record WaitingRequestDto(LocalDate date, Long timeId, Long themeId) {

    public Waiting toEntity(ReservationTime reservationTime, Theme theme, User user) {
        return Waiting.of(date, reservationTime, theme, user);
    }
}
