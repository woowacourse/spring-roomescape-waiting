package roomescape.domain.repository.projection;

import roomescape.domain.Theme;

public class PopularThemeProjection {
    private final Theme theme;
    private final long reservationCount;

    public PopularThemeProjection(Theme theme, long reservationCount) {
        this.theme = theme;
        this.reservationCount = reservationCount;
    }

    public Theme getTheme() {
        return theme;
    }

    public long getReservationCount() {
        return reservationCount;
    }
}
