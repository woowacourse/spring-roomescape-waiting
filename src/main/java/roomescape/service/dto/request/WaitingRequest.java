package roomescape.service.dto.request;

import java.time.DateTimeException;
import java.time.LocalDate;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

public record WaitingRequest(LocalDate date, Long timeId, Long themeId) {
    public WaitingRequest {
        validate(date, timeId, themeId);
    }

    private void validate(LocalDate date, Long timeId, Long themeId) {
        if (date == null || timeId == null || themeId == null) {
            throw new IllegalArgumentException();
        }
    }

    public Waiting toWaiting(Member member, ReservationTime time, Theme theme) {
        return new Waiting(date, member, time, theme);
    }
}
