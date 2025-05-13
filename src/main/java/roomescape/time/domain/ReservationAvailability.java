package roomescape.time.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.theme.domain.Theme;

public final class ReservationAvailability {

    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final boolean isBooked;

    public ReservationAvailability(final LocalDate date, final ReservationTime time, final Theme theme,
                                   final boolean isBooked) {
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.isBooked = isBooked;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public long getTimeId() {
        return time.getId();
    }

    public LocalTime getStartAt() {
        return time.getStartAt();
    }
}
