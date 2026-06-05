package roomescape.service.dto;

import roomescape.repository.projection.PopularThemeProjection;

public class PopularThemeResult {

    private final ThemeResult theme;
    private final long reservationCount;

    public PopularThemeResult(ThemeResult theme, long reservationCount) {
        this.theme = theme;
        this.reservationCount = reservationCount;
    }


    public static PopularThemeResult from(PopularThemeProjection projection) {
        return new PopularThemeResult(
                ThemeResult.from(projection.getTheme()),
                projection.getReservationCount()
        );
    }

    public ThemeResult getTheme() {
        return theme;
    }

    public long getReservationCount() {
        return reservationCount;
    }
}
