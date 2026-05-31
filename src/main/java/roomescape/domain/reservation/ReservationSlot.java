package roomescape.domain.reservation;

import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import java.time.LocalDate;

public class ReservationSlot {

    private final LocalDate date;
    private final Long timeId;
    private final Long themeId;

    public ReservationSlot(LocalDate date, ReservationTime time, Theme theme) {
        this.date = date;
        this.timeId = time.getId();
        this.themeId = theme.getId();
    }

    public ReservationSlot(LocalDate date, Long timeId, Long themeId) {
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationSlot that)) return false;
        return java.util.Objects.equals(date, that.date)
                && java.util.Objects.equals(timeId, that.timeId)
                && java.util.Objects.equals(themeId, that.themeId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(date, timeId, themeId);
    }
}
