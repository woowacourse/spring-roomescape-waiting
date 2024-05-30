package roomescape.service.dto.request;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

public record WaitingRequest(LocalDate date, Long timeId, Long themeId) {
    public WaitingRequest {
        validate(date, timeId, themeId);
    }

    private void validate(Object... values) {
        if (Stream.of(values).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException();
        }
    }

    public Waiting toWaiting(Member member, ReservationTime time, Theme theme) {
        return new Waiting(date, member, time, theme);
    }
}
