package roomescape.service.dto;

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

    public WaitingRequest(String date, String timeId, String themeId) {
        validate(date, timeId, themeId);
        this.date = LocalDate.parse(date);
        this.timeId = Long.parseLong(timeId);
        this.themeId = Long.parseLong(themeId);
    }

    private void validate(String date, String timeId, String themeId) {
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
