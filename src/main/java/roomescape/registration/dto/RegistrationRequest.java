package roomescape.registration.dto;

import java.time.LocalDate;
import roomescape.registration.domain.waiting.domain.Waiting;

public record RegistrationRequest(LocalDate date, long themeId, long timeId, long memberId) {

    public static RegistrationRequest from(Waiting waiting) {
        return new RegistrationRequest(waiting.getDate(), waiting.getTheme().getId(),
                waiting.getReservationTime().getId(), waiting.getMember().getId());
    }
}
