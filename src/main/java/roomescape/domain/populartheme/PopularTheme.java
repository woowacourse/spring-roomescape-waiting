package roomescape.domain.populartheme;

import roomescape.domain.Theme;

public class PopularTheme {

    private final Theme theme;
    private final Long reservationCount;

    public PopularTheme(Theme theme, Long reservationCount) {
        validateTheme(theme);
        validateReservationCount(reservationCount);

        this.theme = theme;
        this.reservationCount = reservationCount;
    }

    public Theme getTheme() {
        return theme;
    }

    public Long getReservationCount() {
        return reservationCount;
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("theme은 비어 있을 수 없습니다.");
        }
    }

    private void validateReservationCount(Long reservationCount) {
        if (reservationCount == null || reservationCount < 0) {
            throw new IllegalArgumentException("reservationCount는 비어 있거나 음수일 수 없습니다.");
        }
    }
}
