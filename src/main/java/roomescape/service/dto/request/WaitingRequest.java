package roomescape.service.dto.request;

import java.time.DateTimeException;
import java.time.LocalDate;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

public class WaitingRequest {

    private final LocalDate date;
    private final Long timeId;
    private final Long themeId;

    public WaitingRequest(String date, Long timeId, Long themeId) {
        validate(date, timeId, themeId);
        this.date = LocalDate.parse(date);
        this.timeId = timeId;
        this.themeId = themeId;
    }

    private void validate(String date, Long timeId, Long themeId) {
        if (date == null || timeId == null || themeId == null) {
            throw new IllegalArgumentException();
        }
        try {
            LocalDate.parse(date);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException();
        }
    }

    public Waiting toWaiting(Member member, ReservationTime time, Theme theme) {
        return new Waiting(date, member, time, theme);
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getTimeId() {
        return timeId;
    }

    public Long getThemeId() {
        return themeId;
    }
}
